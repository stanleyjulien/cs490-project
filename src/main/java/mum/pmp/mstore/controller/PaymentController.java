package mum.pmp.mstore.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mum.pmp.mstore.domain.Order;
import mum.pmp.mstore.model.CreditCard;
import mum.pmp.mstore.service.OrderService;

@Controller
@RequestMapping("/payment")
public class PaymentController {
	@Autowired
	OrderService orderService;

	@PostMapping({ "/" })
	public void processPayment(@PathVariable String orderNumber, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		Order order = (Order) request.getAttribute("order"); //orderService.getOrder(orderNumber);
		if (order != null) {
			String paymentUrl = "";
			String fallbackUrl = ""; // "http://localhost:8080/payment";
			CreditCard fromCard = order.getCreditCard();
			CreditCard toCard;
			if (fromCard.getCardType() == 1) { // Visa
				paymentUrl = "/paymentgw/visa";
				fallbackUrl = fallbackUrl + "/payment/visa/confirm";
				toCard = new CreditCard("5678123412340079", "Company", "02/23", "123");
			} else {
				paymentUrl = "/paymentgw/master";
				fallbackUrl = fallbackUrl + "/payment/master/confirm";
				toCard = new CreditCard("1234123412340079", "Company", "02/23", "123");
			}

			try {
				RequestDispatcher rd = request.getRequestDispatcher(paymentUrl);
				request.setAttribute("fromCardNumber", fromCard.getCardNumber());
				request.setAttribute("fromCardName", fromCard.getCardName());
				request.setAttribute("fromCardCSV", fromCard.getCsv());
				request.setAttribute("fromCardExpireDate", fromCard.getExpireDate());
				request.setAttribute("toCardNumber", toCard.getCardNumber());
				request.setAttribute("toCardName", toCard.getCardName());
				request.setAttribute("toCardCSV", toCard.getCsv());
				request.setAttribute("toCardExpireDate", toCard.getExpireDate());
				request.setAttribute("orderNumber", order.getOrdernumber());
				request.setAttribute("amount", order.getTotalPrice());
				// request.setAttribute("fromCardNumber", "1234123412340001");
				// request.setAttribute("fromCardName", "Yee Mon");
				// request.setAttribute("fromCardCSV", "123");
				// request.setAttribute("fromCardExpireDate", "02/23");
				// request.setAttribute("orderNumber", "1");
				// request.setAttribute("amount", 1000.00);
				request.setAttribute("fallbackUrl", fallbackUrl);
				rd.forward(request, response);
			} catch (ServletException | IOException e) {
				System.out.println(e.getMessage());
				// e.printStackTrace();
			}
		}
	}

	@PostMapping("/{type}/confirm")
	public void paymentFallBack(@PathVariable String type, RedirectAttributes redirectAttributes,
			HttpServletRequest request, HttpServletResponse response) {
		String status = (String) request.getAttribute("status");
		String orderNumber = (String) request.getAttribute("orderNumber");
		System.out.println("Fall back from payment gateway..." + status + "," + orderNumber);
		// redirectAttributes.addFlashAttribute("orderNumber", orderNumber);
		try {
			String targetURL = "";
			if (status.equals("approved")) {
				Order order = orderService.getOrder(orderNumber);
				request.setAttribute("order", order);
				targetURL = "/settlement";
			} else {
				request.setAttribute("status", status);
				targetURL = "/payment";
			}
			RequestDispatcher rd = request.getRequestDispatcher(targetURL);
			rd.forward(request, response);
		} catch (ServletException | IOException e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
		}
	}

	@PostMapping("")
	public String paymentErrorHandler(RedirectAttributes redirectAttributes, HttpServletRequest request,
			HttpServletResponse response) {
		redirectAttributes.addFlashAttribute("status", (String) request.getAttribute("status"));
		return "redirect:/payment";
	}

	@GetMapping("")
	public String displayError(@ModelAttribute("status") String status, Model model) {
		System.out.println("Get back from payment gateway..." + status);
		if (status.equals("approved")) {
			model.addAttribute("status", "Order successfull!");
			model.addAttribute("message", "Thank you for shopping at Online Branded Shopping.");
//			model.addAttribute("detail", "Your order number is: " + orderNumber);
		} else {
			model.addAttribute("status", "Order failed!");
			model.addAttribute("message", "Payment is not approved.");
			model.addAttribute("detail", "Please recheck your payment information");
		}

		return "payment_error";
	}
	
}
