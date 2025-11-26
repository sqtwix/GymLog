using GymLogServer.Context;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Добавляем контроллеры и OpenAPI
builder.Services.AddControllers();
builder.Services.AddOpenApi();

builder.Services.AddEndpointsApiExplorer();   

// КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: берём connection string из переменной окружения!
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
                    ?? Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
                    ?? throw new InvalidOperationException("Connection string not found!");

builder.Services.AddDbContext<GymLogContext>(options =>
    options.UseNpgsql(connectionString));

// JWT
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes("SuperSecretKey12345"))
        };
    });

var app = builder.Build();

// Автомиграции только в Production (т.е. в Docker)
if (app.Environment.IsProduction())
{
    try
    {
        using var scope = app.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<GymLogContext>();
        app.Logger.LogInformation("Применяем миграции...");
        db.Database.Migrate();
        app.Logger.LogInformation("Миграции успешно применены!");
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "ОШИБКА ПРИ ПРИМЕНЕНИИ МИГРАЦИЙ!");
        throw;
    }
}

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();