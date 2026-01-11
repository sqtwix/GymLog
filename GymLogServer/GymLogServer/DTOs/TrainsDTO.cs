using System.ComponentModel.DataAnnotations;

namespace GymLogServer.DTOs
{
    public class TrainsDTO
    {
        [Required(ErrorMessage = "Type must be")]
        public string Type { get; set; }
        public string Description { get; set; }
        public DateTime Date { get; set; }
        [Range(1, int.MaxValue, ErrorMessage = "Duration must be greater than 0")]
        public int Duration { get; set; }
        public int UserId { get; set; }
    }
}
