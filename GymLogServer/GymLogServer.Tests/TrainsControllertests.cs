using GymLogServer.Controllers;
using GymLogServer.Context;
using GymLogServer.DTOs;
using GymLogServer.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using System.Text.Json;


namespace GymLogServer.Tests
{
    public class TrainsControllerTests
    {
        private GymLogContext GetDbContext()
        {
            var options = new DbContextOptionsBuilder<GymLogContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // Уникальное имя для каждого теста
                .Options;

            var context = new GymLogContext(options);
            context.Database.EnsureCreated();
            return context;
        }

        [Fact]
        public async Task MakeTrain_NewTrain_Success()
        {
            GymLogContext context = GetDbContext();

            var controller = new TrainsController(context);

            var user = new User
            {
                Username = "TestUser",
                Email = "test@example.com",
                PasswordHash = "123",
                Gender = "male",
                BirthDay = DateTime.UtcNow
            };

            context.Users.Add(user);
            await context.SaveChangesAsync();

            var trainDto = new TrainsDTO
            {
                Type = "Тренажерный зал",
                Description = "Тренировка груди, рук",
                Date = DateTime.UtcNow,
                Duration = 90,
                UserId = user.Id,
            };

            var result = await controller.MakeTrain(trainDto);

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.NotNull(okResult.Value);
        }



        [Fact]
        public async Task GetTrains_ReturnsOk_WithTrainsList()
        {
            // Arrange
            var context = GetDbContext();

            var user = new User
            {
                Username = "TestUser",
                Email = "test@example.com",
                PasswordHash = "123",
                Gender = "male",
                BirthDay = DateTime.UtcNow
            };

            user.Trains.Add(new Train
            {
                Type = "Legs",
                Description = "Squats",
                Date = DateTime.UtcNow.AddDays(-1),
                Duration = 60
            });

            user.Trains.Add(new Train
            {
                Type = "Arms",
                Description = "Biceps curls",
                Date = DateTime.UtcNow,
                Duration = 45
            });

            context.Users.Add(user);
            await context.SaveChangesAsync();

            var controller = new TrainsController(context);

            // Act - используем реальный ID созданного пользователя
            var result = await controller.GetTrains(user.Id);

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            var trains = Assert.IsAssignableFrom<IEnumerable<object>>(okResult.Value);
            Assert.Equal(2, trains.Count());
        }
        [Fact]
        public async Task GetTrains_UserNotFound_ReturnsNotFound()
        {
            // Arrange
            var context = GetDbContext();
            var controller = new TrainsController(context);

            // Act - пытаемся получить тренировки несуществующего пользователя
            var result = await controller.GetTrains(999);

            // Assert
            Assert.IsType<NotFoundObjectResult>(result);
        }

        [Fact]
        public async Task MakeTrain_UserNotFound_ReturnsBadRequest()
        {
            // Arrange
            var context = GetDbContext();
            var controller = new TrainsController(context);

            var dto = new TrainsDTO
            {
                Type = "Тренажерный зал",
                Description = "Тренировка груди, рук",
                Date = DateTime.UtcNow,
                Duration = 90,
                UserId = 999 // Несуществующий пользователь
            };

            // Act
            var result = await controller.MakeTrain(dto);

            // Assert
            var badRequest = Assert.IsType<BadRequestObjectResult>(result);
            Assert.NotNull(badRequest.Value);
        }
    }
}

