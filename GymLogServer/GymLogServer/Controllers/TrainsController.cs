using GymLogServer.Context;
using GymLogServer.DTOs;
using GymLogServer.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;


namespace GymLogServer.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class TrainsController : ControllerBase
    {
        private readonly GymLogContext _context;

        public TrainsController(GymLogContext context)
        {
            _context = context;
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
    }
}