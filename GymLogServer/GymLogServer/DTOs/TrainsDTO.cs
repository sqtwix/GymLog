namespace GymLogServer.DTOs
{
    public class TrainsDTO
    {
        public string Type { get; set; }
        public string Description { get; set; }
        public DateTime Date { get; set; }
        public int Duration { get; set; }
        public int UserId { get; set; }
    }
}
