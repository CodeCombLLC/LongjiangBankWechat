﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel.DataAnnotations;

namespace LongjiangBank.Models
{
    public class Production
    {
        public Guid Id { get; set; }

        [MaxLength(64)]
        public string Title { get; set; }

        public string Description { get; set; }

        public long Cost { get; set; }

        public bool IsBan { get; set; }

        public long ExchangeCount { get; set; }

        public byte[] Picture { get; set; }

        public string ContentType { get; set; }
    }
}
