using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Mvc;
using Microsoft.AspNet.Http;
using LongjiangBank.Models;

namespace LongjiangBank.Controllers
{
    public class BaseController : BaseController<BankContext>
    {
        private Customer customer = null;

        public Customer Customer { get { return customer; } }

        public override void Prepare()
        {
            base.Prepare();

            var uid = HttpContext.Session.GetString("uid");
            if (!string.IsNullOrEmpty(uid))
            {
                customer = DB.Customers.Where(x => x.Id == uid).SingleOrDefault();
            }
        }
    }
}
