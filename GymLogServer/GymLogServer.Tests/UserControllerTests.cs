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
    public class UserControllerTests
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
        public async void Register_ShouldCreateUser()
        {
            GymLogContext context = GetDbContext();

            // Очистка базы
            context.Users.RemoveRange(context.Users);
            await context.SaveChangesAsync();

            var controller = new UserController(context);

            var dto = new RegisterDTO
            {
                Name = "Test",
                Email = "test@example.com",
                Password = "password123",
                Gender = "Male",
                BirthDay = DateTime.UtcNow // Измените на BirthDay
            };

            var result = await controller.Register(dto);

            if (result is BadRequestObjectResult badRequest)
            {
                var errorMessage = badRequest.Value?.ToString();
                throw new Exception($"Registration failed with error: {errorMessage}");
            }

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.NotNull(okResult.Value);
        }

        [Fact]
        public async Task Login_ShouldEnter()
        {
            // Arrange
            var context = GetDbContext();
            var controller = new UserController(context);

            // Сначала регистрируем пользователя
            var registerDto = new RegisterDTO
            {
                Name = "TestUser",
                Email = "test@example.com",
                Password = "password123",
                Gender = "Male",
                BirthDay = DateTime.UtcNow
            };
            await controller.Register(registerDto);

            // Теперь пытаемся войти
            var loginDto = new LoginDTO
            {
                Email = "test@example.com",
                Password = "password123"
            };

            // Act
            var result = await controller.Login(loginDto);

            // Assert - ожидаем успешный вход
            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.NotNull(okResult.Value);

            // Безопасная проверка через сериализацию
            var json = JsonSerializer.Serialize(okResult.Value);
            Assert.Contains("Token", json);
            Assert.Contains("User", json);
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
