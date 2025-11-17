using System;
using System.ComponentModel.DataAnnotations;

namespace GymLogServer.DTOs
{
    public class RegisterDTO
    {
        [Required(ErrorMessage = "Имя обязательно")]
        [MinLength(2, ErrorMessage = "Имя должно быть длиннее 2 символов")]
        public string Name { get; set; } = "";

        [Required]
        [EmailAddress(ErrorMessage = "Некорректный email")]
        public string Email { get; set; } = "";

        [Required, MinLength(6)]
        public string Password { get; set; } = "";

        public string Gender { get; set; } = "";

        [DataType(DataType.Date)]
        public DateTime BirthDay { get; set; }
    }
}
