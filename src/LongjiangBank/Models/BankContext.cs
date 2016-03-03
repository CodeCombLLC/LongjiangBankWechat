using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Data.Entity;

namespace LongjiangBank.Models
{
    public class BankContext : DbContext
    {
        public DbSet<Customer> Customers { get; set; }

        public DbSet<Deposit> Deposits { get; set; }

        public DbSet<Production> Productions { get; set; }

        public DbSet<Exchange> Exchanges { get; set; }

        protected override void OnModelCreating(ModelBuilder builder)
        {
            base.OnModelCreating(builder);

            builder.Entity<Deposit>(e => 
            {
                e.HasIndex(x => x.SubmitTime);
                e.HasIndex(x => x.VerifyTime);
            });

            builder.Entity<Production>(e => 
            {
                e.HasIndex(x => x.Title);
                e.HasIndex(x => x.Cost);
                e.HasIndex(x => x.IsBan);
            });

            builder.Entity<Exchange>(e => 
            {
                e.HasIndex(x => x.Time);
                e.HasIndex(x => x.DistributeTime);
                e.HasIndex(x => x.IsDistributed);
            });
        }
    }
}
