﻿using GymLogServer.Models;

namespace GymLogServer.DTOs
{
    public class RefreshTokenDTO
    {
        public int UserId { get; set; }
        public string Token { get; set; } = "";
        public DateTime Expires { get; set; }
        public bool IsRevoked { get; set; }
    }
}
