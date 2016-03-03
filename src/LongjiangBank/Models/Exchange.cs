using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel.DataAnnotations.Schema;

namespace LongjiangBank.Models
{
    public class Exchange
    {
        public Guid Id { get; set; }

        public DateTime Time { get; set; }

        [ForeignKey("Production")]
        public Guid ProductionId { get; set; }

        public Production Production { get; set; }

        public bool IsDistributed { get; set; }

        public DateTime? DistributeTime { get; set; }
    }
}
