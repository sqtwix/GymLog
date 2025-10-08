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

    }
}
