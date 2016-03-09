using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LongjiangBank.Models
{
    public enum DepositStatus
    {
        待兑换,
        审核中,
        兑换失败,
        兑换成功
    }

    public class Deposit
    {
        [MaxLength(128)]
        public string Id { get; set; }

        [MaxLength(32)]
        public string Name { get; set; }

        [MaxLength(32)]
        public string PRCID { get; set; }

        public DepositStatus Status { get; set; }

        public DateTime SubmitTime { get; set; }

        public DateTime? VerifyTime { get; set; }

        public string Hint { get; set; }

        public long Coins { get; set; }

        [ForeignKey("Customer")]
        public string CustomerId { get; set; }

        public Customer Customer { get; set; }
    }
}
