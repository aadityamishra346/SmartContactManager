package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;
import com.sun.el.stream.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ContactRepository contactRepository;
	
	
	
	//method for adding common data response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		String userName = principal.getName();
		System.out.println("USERNAME" +userName);

		//get the user using username(Email)
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER" +user);
		model.addAttribute("user", user);
		
	}
	
	
	//Dashboard home
	
	@RequestMapping("/index")
	public String dashboard(Model model , Principal principal) {
		
		model.addAttribute("title", "User_Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	// open add form handler
	
	@GetMapping("/add_contact")
	public String openContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	
	//processing add contact form
	
	@PostMapping("/procss_contact")
	
	public String processContact( @ModelAttribute  @RequestParam("profileImage") MultipartFile file,
									Contact contact,Principal principal, HttpSession session ) {
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		// processing  and Uploading file 
		
		if(file.isEmpty()) {
			
			// if file is Empty try your message
			System.out.println("Image is Empty");
			contact.setImage("contact.png");
		}
		else {
			// file the file to folder and upload the name to contact
			
			contact.setImage(file.getOriginalFilename());
			
			File savefile		=	new ClassPathResource("static/img").getFile();
			
		Path path = 	Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is Uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA" +contact);
		System.out.println("Added to database");
		
		// message - Success
		
		session.setAttribute("message", new Message("Your Contact is Added !! Add more..", "success"));
		
		System.out.println("message"+session);
		
		}
		catch (Exception e) {
			
			System.out.println(e.getMessage());
			
			//message -error
			
			session.setAttribute("message", new Message("Something went Wrong !! Try again..", "danger"));
			System.out.println(session);
		}
		return "normal/add_contact_form";
	}
	
	
	// shOW CONTATACT HANDLER
	
	//per page = 5[n]
	// current page = 0[page]
	
	
	@GetMapping("show_contacts/{page}")
		public String showContacts(@PathVariable("page") Integer page, Model model , Principal principal) {
			model.addAttribute("title", "Show User Contacts");
			String userName= principal.getName();
		User user=	this.userRepository.getUserByUserName(userName);
		
		//per page = 5[n]
		// current page = 0[page]
		Pageable pageable	= PageRequest.of(page, 5);
		
			// send contact list 
			
		Page<Contact> contacts =	this.contactRepository.findContactByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalpages",contacts.getTotalPages());
			return "normal/show_contacts";
		}
	
	
	// showing particular contact details...
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cid, Model model, Principal principal) {
		
		System.out.println("CID-" +cid);
		
		java.util.Optional<Contact> contacOptional =	this.contactRepository.findById(cid);
		Contact contact = contacOptional.get();
		
		//
		
	String userName = principal.getName();
	User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()== contact.getUser().getId()) {
			
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	
	//delete contact Handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model , HttpSession session, Principal principal) {
		
		java.util.Optional<Contact> contactOptional	= this.contactRepository.findById(cId);
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		// check..Assignment.
		System.out.println("COntact" +contact.getcId());
		
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		
		
		
		
		session.setAttribute("message", new Message("Contact deleted Successfully", "success"));
		
		return "redirect:/user/show_contacts/0";
	}
	
	
	
	//Open Update form
	
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,  Model model) {
		
		model.addAttribute("title","Update_Form");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	
	//update contact handler
	
	@PostMapping("/procss_update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {
		
		try {
			// old contact details
			Contact oldContactDetail =	this.contactRepository.findById(contact.getcId()).get();
			
			
			//imgae select new
			if(! file.isEmpty()) {
				//file work....REWRITE
				//delete old photo
				File deleteFile	=	new ClassPathResource("static/img").getFile();
				File file1 = new  File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				
				File savefile		=	new ClassPathResource("static/img").getFile();
				
				Path path = 	Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("your contact has been updated...", "success"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact Name" +contact.getName());
		System.out.println("Contact id "+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	
	//Your Profile Handler
	
	@GetMapping("/profile")
	public String youProfile(Model model) {
		
		model.addAttribute("title","Profle Page");
		return "normal/profile";
	}
	
	
}
