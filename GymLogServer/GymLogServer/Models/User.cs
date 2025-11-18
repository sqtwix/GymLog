namespace GymLogServer.Models
{
    public class User
    {
        public int Id { get; set; }
        public required string Username { get; set; }
        public required string PasswordHash { get; set; }
        public required string Email { get; set; }
        public DateTime BirthDay { get; set; }
        public required string Gender { get; set; }

        public List<RefreshToken> RefreshTokens { get; set; } = new();
        public List<Train> Trains { get; set; } = new();
    }
}
