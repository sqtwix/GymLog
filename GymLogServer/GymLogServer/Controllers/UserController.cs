using GymLogServer.Models;
using Microsoft.AspNetCore.Mvc;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using GymLogServer.Context;
using GymLogServer.DTOs;
using System.Security.Claims;


namespace GymLogServer.Controllers
{

    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        /* User controller 
            Methods:
                Register - method for inserting user in DB
                Login - method for sending JWT and user`s data
                Refresh - method for sending refresh token
                GenerateRefreshToken - method for generating refresh token
                HashPassword - method for making hash from current user password
                GenerateJwtToken - method for generating JWT
        */

        private readonly GymLogContext _context;

        public UserController(GymLogContext context)
        {
            _context = context;
        }


        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDTO dto)
        {
            if (!ModelState.IsValid)  
                return BadRequest(ModelState); 

            if (await _context.Users.AnyAsync(u => u.Email == dto.Email))
                return BadRequest("Пользователь с таким email уже существует");

            var user = new User
            {
                Username = dto.Name,
                Email = dto.Email,
                PasswordHash = HashPassword(dto.Password),
                Gender = dto.Gender,
                BirthDay = DateTime.SpecifyKind(dto.BirthDay, DateTimeKind.Utc)
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return Ok(new { user.Id, user.Username, user.Email, user.Gender, user.BirthDay });
        }


        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDTO dto)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
            if (user == null)
                return BadRequest("Пользователь не найден");

            if (_context == null)
                return StatusCode(500, "Database context not available");

            if (User == null)
                return Unauthorized("User not authenticated");

            var hashedPassword = HashPassword(dto.Password);
            if (user.PasswordHash != hashedPassword)
                return BadRequest("Неверный email или пароль");

            var jwtToken = GenerateJwtToken(user); // Create JWT Token

            var refreshToken = new RefreshToken // Create Refresh Token
            {
                Token = Convert.ToBase64String(RandomNumberGenerator.GetBytes(32)),
                UserId = user.Id,
                Expires = DateTime.UtcNow.AddDays(7),
                IsRevoked = false
            };
            _context.RefreshTokens.Add(refreshToken);
            await _context.SaveChangesAsync();

            return Ok(new { 
                Token = jwtToken,
                User = new { user.Id, user.Username, user.Email }
            });
        }


        [HttpPost("refresh")]
        public async Task<IActionResult> Refresh([FromBody] string refreshToken)
        {
            var tokenInDb = await _context.RefreshTokens
           .Include(t => t.User)
           .FirstOrDefaultAsync(t => t.Token == refreshToken && !t.IsRevoked && t.Expires > DateTime.UtcNow);

            if (tokenInDb == null)
                return Unauthorized("Неверный или просроченный токен");

            var newAccessToken = GenerateJwtToken(tokenInDb.User); // New Access Token

            tokenInDb.IsRevoked = true; // Old Refresh Token is revoked
            var newRefreshToken = GenerateRefreshToken(); // New Refresh Token
            tokenInDb.User.RefreshTokens.Add(new RefreshToken // Add new Refresh Token to DB
            {
                Token = newRefreshToken,
                Expires = DateTime.UtcNow.AddDays(7)
            });
            await _context.SaveChangesAsync();

            return Ok(new
            {
                Token = newAccessToken,
                RefreshToken = newRefreshToken
            });
        }


        private string GenerateRefreshToken()
        {
            var randomNumber = new byte[32];
            using var rng = RandomNumberGenerator.Create();
            rng.GetBytes(randomNumber);
            return Convert.ToBase64String(randomNumber);
        }


        private string HashPassword(string password)
        {
            using var sha256 = SHA256.Create();
            var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));

            return Convert.ToBase64String(bytes);
        }

        private string GenerateJwtToken(User user)
        {
            var tokenHandler = new JwtSecurityTokenHandler();
    
            // ИСПОЛЬЗУЕМ ТОТ ЖЕ КЛЮЧ, ЧТО И В PROGRAM.CS
            var key = Encoding.UTF8.GetBytes("MySuperSecretKeyForGymLogApp1234567890!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Username ?? "User"),
                new Claim(ClaimTypes.Email, user.Email)
            };

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = DateTime.UtcNow.AddHours(12),
                SigningCredentials = new SigningCredentials(
                    new SymmetricSecurityKey(key),
                    SecurityAlgorithms.HmacSha256Signature
                )
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }
    }
}
