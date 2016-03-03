using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Builder;
using Microsoft.AspNet.Hosting;
using Microsoft.AspNet.Http;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Data.Entity;
using LongjiangBank.Models;

namespace LongjiangBank
{
    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMvc();

            services.AddEntityFramework()
                .AddDbContext<BankContext>(x => x.UseSqlite("Data source=ljbank.db"))
                .AddSqlite();

            services.AddCaching();
            services.AddSession(x => x.IdleTimeout = TimeSpan.FromMinutes(20));
        }

        public void Configure(IApplicationBuilder app, ILoggerFactory logger)
        {
            logger.MinimumLevel = LogLevel.Debug;
            logger.AddConsole();

            app.UseSession();
            app.UseStaticFiles();
            app.UseIISPlatformHandler();
            app.UseMvcWithDefaultRoute();

            SampleData.InitDB(app.ApplicationServices);
        }

        public static void Main(string[] args) => WebApplication.Run<Startup>(args);
    }
}
