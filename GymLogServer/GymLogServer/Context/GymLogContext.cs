using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using GymLogServer.Models;
using Microsoft.EntityFrameworkCore.Design;


namespace GymLogServer.Context;

public class GymLogContext : DbContext
{
    public GymLogContext(DbContextOptions<GymLogContext> options)
    : base(options)
    {
    }

    public DbSet<User> Users { get; set; } = null!;
    public DbSet<RefreshToken> RefreshTokens { get; set; } = null!;


}
