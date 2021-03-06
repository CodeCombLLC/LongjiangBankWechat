﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel.DataAnnotations;

namespace LongjiangBank.Models
{
    public class Customer
    {
        [MaxLength(128)]
        public string Id { get; set; }

        public long Coins { get; set; }

        public string PRCID { get; set; }

        public string Name { get; set; }

        public string Phone { get; set; }

        public string Password { get; set; }

        public virtual ICollection<Deposit> Deposits { get; set; } = new List<Deposit>();
    }
}
