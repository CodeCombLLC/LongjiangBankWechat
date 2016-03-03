using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.DependencyInjection;

namespace LongjiangBank.Models
{
    public class SampleData
    {
        public static void InitDB(IServiceProvider services)
        {
            var DB = services.GetRequiredService<BankContext>();
            DB.Database.EnsureCreated();
        }
    }
}
