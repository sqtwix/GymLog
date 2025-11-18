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
                return BadRequest("Не получилось найти пользователя!");

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
        public async Task<IActionResult> GetTrains(int userId)  // Get all trains for user
        {
            var trains = await _context.Trains
             .Where(t => t.UserId == userId)
             .Select(t => new
             {
                 t.Type,
                 t.Description,
                 t.Date,
                 t.Duration
             })
             .OrderBy(t => t.Date)
             .ToListAsync();

            if (!trains.Any())
                return NotFound("Не получилось найти тренировки");

            return Ok(trains);
        }

        [HttpDelete("delete/{userId}/{selectedDate}")]
        public async Task<IActionResult> DeleteTrain(int userId, DateTime selectedDate)
        {
            var date = DateTime.SpecifyKind(selectedDate.Date, DateTimeKind.Utc);

            var trainToDelete = await _context.Trains
                .Where(t => t.UserId == userId && t.Date.Date == date.Date)
                .ToListAsync();

            if (trainToDelete.Count == 0)
                return NotFound("Не получилось найти на эту дату");

            _context.Trains.RemoveRange(trainToDelete);

            await _context.SaveChangesAsync();

            return Ok(new
            {
                Deleted = trainToDelete.Count,
                Date = date
            });
        }
    }
}