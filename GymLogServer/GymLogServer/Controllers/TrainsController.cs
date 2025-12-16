using GymLogServer.Context;
using GymLogServer.DTOs;
using GymLogServer.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace GymLogServer.Controllers
{
    [Route("api/trains")]
    [ApiController]
    [Authorize]
    public class TrainsController : ControllerBase
    {
        /* Trains controller 
            Methods:
                GetTrains - method for sending all trains of current user
                MakeTrain - method for inserting new train in DB
                DeleteTrain - method for deleting train by choosen date
        */

        private readonly GymLogContext _context;

        public TrainsController(GymLogContext context)
        {
            _context = context;
        }


        [HttpGet]
        public async Task<IActionResult> GetTrains()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null || !int.TryParse(userIdClaim.Value, out int userId))
                return Unauthorized("Invalid token");

            var trains = await _context.Trains
                .Where(t => t.UserId == userId)
                .Select(t => new
                {
                    t.Id,
                    t.Type,
                    t.Description,
                    t.Date,
                    t.Duration
                })
                .OrderBy(t => t.Date)
                .ToListAsync();

            return Ok(trains);
        }


        [HttpPost("train")]
        public async Task<IActionResult> MakeTrain([FromBody] TrainsDTO dto)
        {
            if (dto == null) return BadRequest("Payload is null");

            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null || !int.TryParse(userIdClaim.Value, out int userId))
                return Unauthorized("Invalid token");

            var user = await _context.Users.FindAsync(userId); 
            if (user == null)
            {
                return BadRequest("User not found");
            }

            var train = new Train
            {
                Type = dto.Type,
                Description = dto.Description,
                Date = DateTime.SpecifyKind(dto.Date, DateTimeKind.Utc),
                Duration = dto.Duration,
                UserId = userId
            };

            _context.Trains.Add(train);
            await _context.SaveChangesAsync();

            return Ok(new { train.Id, train.Type, train.Description, train.Date, train.Duration });
        }


        [HttpDelete("delete/{date}")]
        public async Task<IActionResult> DeleteTrain(DateTime date)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null || !int.TryParse(userIdClaim.Value, out int userId))
                return Unauthorized("Invalid token");

            var targetDate = DateTime.SpecifyKind(date.Date, DateTimeKind.Utc);
            var trainsToDelete = await _context.Trains
                .Where(t => t.UserId == userId && t.Date.Date == targetDate)
                .ToListAsync();

            if (!trainsToDelete.Any())
                return NotFound("No trains found for this date");

            _context.Trains.RemoveRange(trainsToDelete);
            await _context.SaveChangesAsync();

            return Ok(new { message = $"Deleted {trainsToDelete.Count} trains" });
        }
    }
}