namespace GymLogServer.Models
{
    public class Train
    {
        public int Id { get; set; }
        public required string Type { get; set; }
        public required string Description { get; set; }
        public DateTime Date { get; set; }
        public int Duration { get; set; } 
        public int UserId { get; set; }
        public User User { get; set; }
    }
}
