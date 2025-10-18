using GymLogServer.Models;
using Microsoft.AspNetCore.Mvc;
using System.Text;
using Microsoft.AspNetCore.Authorization;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using GymLogServer.Context;
using GymLogServer.DTOs;
using System.Security.Claims;


// For more information on enabling Web API for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace GymLogServer.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {

        private readonly GymLogContext _context;

        public UserController(GymLogContext context)
        {
            _context = context;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDTO dto)
        {
            if (!ModelState.IsValid)  // Correct data or not
                return BadRequest(ModelState); 

            if (await _context.Users.AnyAsync(u => u.Email == dto.Email))
                return BadRequest("Пользователь с таким email уже существует");

            var user = new User
            {
                Username = dto.Name,
                Email = dto.Email,
                PasswordHash = HashPassword(dto.Password),
                Gender = dto.Gender,
                BirthDay = DateTime.SpecifyKind(dto.Birthday, DateTimeKind.Utc)
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

        [HttpPost("train")]
        public async Task<IActionResult> MakeTrain([FromBody] TrainsDTO dto)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Id == dto.UserId);
            if (user == null)
                return BadRequest("Пользователь не найден");

            var train = new Train
            {
                Type = dto.Type,
                Description = dto.Description,
                Date = DateTime.SpecifyKind(dto.Date, DateTimeKind.Utc),
                Duration = dto.Duration,
                UserId = dto.UserId
            };

            _context.Trains.Add(train);
            await _context.SaveChangesAsync();

            return Ok(new { train.Id, train.Type, train.Description, train.Date, train.Duration, train.UserId });
        }

        [HttpGet("trains/{userId}")]
        public async Task<IActionResult> GetTrains(int userId)
        {
            var user = await _context.Users
            .Include(u => u.Trains)
            .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
                return NotFound("Пользователь не найден");

            var trains = user.Trains.Select(t => new
            {
                t.Type,
                t.Description,
                t.Date,
                t.Duration
            }).OrderBy(t => t.Date);

            return Ok(trains);
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
            var key = Encoding.UTF8.GetBytes("SuperSecretKey1234567890_!@#JWT_KEY_2025"); // должен совпадать с ключом в Program.cs

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Username),
                new Claim(ClaimTypes.Email, user.Email)
            };

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = DateTime.UtcNow.AddHours(1), // токен живёт 1 час
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }
    }
}
