namespace GymLogServer.Models
{
    public class User
    {
        public int Id { get; set; }
        public string Username { get; set; }
        public string PasswordHash { get; set; }
        public string Email { get; set; }
        public DateTime BirthDay { get; set; }
        public string Gender { get; set; }

        public List<RefreshToken> RefreshTokens { get; set; } = new();
        public List<Train> Trains { get; set; } = new();
    }
}
