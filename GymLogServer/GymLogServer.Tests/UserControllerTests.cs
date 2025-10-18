using GymLogServer.Controllers;
using GymLogServer.Context;
using GymLogServer.DTOs;
using GymLogServer.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Xunit;
using System.Security.Cryptography;

namespace GymLogServer.Tests
{
    public class UserControllerTests
    {
        private GymLogContext GetDbContext() // Inicialization of the test database
        {
            var options = new DbContextOptionsBuilder<GymLogContext>()
         .UseNpgsql("Host=localhost;Port=5432;Database=gymlogdb;Username=postgres;Password=123;Include Error Detail=true")
         .Options;

          var context = new GymLogContext(options);
          context.Database.EnsureCreated();
          return context;
        }

        [Fact]
        public async void Register_ShouldCreateUser()
        {
            GymLogContext context = GetDbContext();
            var controller = new UserController(context);

            var dto = new RegisterDTO
            {
                Name = "Test",
                Email = "test@example.com",
                Password = "password123",
                Gender = "Male",
                Birthday = DateTime.UtcNow
            };

            var result = await controller.Register(dto);

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.NotNull(okResult.Value);
        }

        [Fact]
        public async void Register_ShortPassword_ShouldReturnBadRequest()
        {
            GymLogContext context = GetDbContext();
            var controller = new UserController(context);

            var dto = new RegisterDTO
            {
                Name = "TestUser",
                Email = "test@example.com",
                Password = "p",
                Gender = "Male",
                Birthday = DateTime.UtcNow
            };

            var result = await controller.Register(dto);

            var badRequest = Assert.IsType<BadRequestObjectResult>(result);
            Assert.NotNull(badRequest.Value);
        }

        [Fact]
        public async void Login_ShouldEnter()
        {
            GymLogContext context = GetDbContext();
            var controller = new UserController(context);

            var dto = new RegisterDTO
            {
                
                Email = "test@example.com",
                Password = "password123",
               
            };

            var result = await controller.Register(dto);

            var okRequest = Assert.IsType<BadRequestObjectResult>(result);
            Assert.NotNull(okRequest.Value);
        }

        [Fact]
        public async void Login_IncorrectPassword_ShouldReturnBadRequest()
        {
            GymLogContext context = GetDbContext();
            var controller = new UserController(context);

            var dto = new RegisterDTO
            {

                Email = "test@example.com",
                Password = "passw",

            };

            var result = await controller.Register(dto);

            var badRequest = Assert.IsType<BadRequestObjectResult>(result);
            Assert.NotNull(badRequest.Value);
        }

        [Fact]
        public async Task Refresh_ValidToken_ShouldReturnNewTokens()
        {
            var context = GetDbContext();
            var controller = new UserController(context);

            var user = new User
            {
                Username = "TestUser",
                Email = "test@example.com",
                PasswordHash = "hashed",
                Gender = "Male",
                BirthDay = DateTime.UtcNow
            };
            context.Users.Add(user);
            await context.SaveChangesAsync();

            var refreshToken = new RefreshToken
            {
                Token = Convert.ToBase64String(RandomNumberGenerator.GetBytes(32)),
                UserId = user.Id,
                Expires = DateTime.UtcNow.AddDays(7),
                IsRevoked = false
            };
            context.RefreshTokens.Add(refreshToken);
            await context.SaveChangesAsync();

            var result = await controller.Refresh(refreshToken.Token);

            // Assert
            if (result is OkObjectResult okResult)
            {
                    Assert.NotNull(okResult.Value);

                    var json = okResult.Value.ToString();
                    Assert.Contains("Token", json);
                    Assert.Contains("RefreshToken", json);

                    var updatedToken = await context.RefreshTokens.FirstAsync();
                    Assert.True(updatedToken.IsRevoked);
            }
            else
            {
                var typeName = result.GetType().Name;
                var message = (result as ObjectResult)?.Value?.ToString();
                Assert.True(false, $"Expected OkObjectResult but got {typeName}. Message: {message}");
            }
        }

        [Fact]
        public async Task Refresh_InvalidToken_ShouldReturnUnauthorized()
        {
            // Arrange
            var context = GetDbContext();
            var controller = new UserController(context);

            // Act
            var result = await controller.Refresh("invalid_token");

            // Assert
            Assert.IsType<UnauthorizedObjectResult>(result);
        }

        [Fact]
        public async Task MakeTrain_NewTrain_Success()
        {
            GymLogContext context = GetDbContext();
            var controller = new UserController(context);

            var dto = new TrainsDTO
            {
                Type = "Тренажерный зал",
                Description = "Тренировка груди, рук",
                Date = DateTime.UtcNow,
                Duration = 90,
                UserId = 1
            };

            var result = await controller.MakeTrain(dto);

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.NotNull(okResult.Value);
   
        }

       

        [Fact]
        public async Task GetTrains_ReturnsOk_WithTrainsList()
        {
            var context = GetDbContext();

            var user = new User
            {
                Id = 10,
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

            var controller = new UserController(context);

            // Act
            var result = await controller.GetTrains(1);

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            var trains = Assert.IsAssignableFrom<IQueryable<object>>(okResult.Value);

            Assert.Equal(2, trains.Count());
        }

    }
}
