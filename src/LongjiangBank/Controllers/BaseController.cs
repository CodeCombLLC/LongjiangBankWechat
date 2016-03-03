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

        [NonAction]
        protected IActionResult _Prompt(Action<Prompt> setupPrompt)
        {
            var prompt = new Prompt();
            setupPrompt(prompt);
            Response.StatusCode = prompt.StatusCode;
            return View("_Prompt", prompt);
        }

        public override void Prepare()
        {
            base.Prepare();

            var uid = HttpContext.Session.GetString("uid");
            if (!string.IsNullOrEmpty(uid))
            {
                customer = DB.Customers.Where(x => x.Id == uid).SingleOrDefault();
            }

            ViewBag.NewCoins = DB.Deposits.Where(x => x.Status == DepositStatus.兑换中).Count();
        }
    }
}
