package controllers;

import java.util.*;

import play.*;
import play.mvc.*;
import play.data.*;
import play.db.jpa.*;

import views.html.*;

import models.*;

import kjetland.gtengineplay.gte;


/**
 * Manage a database of computers
 */
public class Application extends Controller {
    
    /**
     * This result directly redirect to application home.
     */
    public static Result GO_HOME = redirect(
        routes.Application.list(0, "name", "asc", "")
    );
    
    /**
     * Handle default path requests, redirect to computers list
     */
    public static Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of computers.
     *
     * @param page Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    @Transactional(readOnly=true)
    public static Result list(int page, String sortBy, String order, String filter) {
        return ok(
              gte.template("list.html")
                .addParam("currentPage", Computer.page(page, 10, sortBy, order, filter))
                .addParam("currentSortBy", sortBy)
                .addParam("currentOrder", order)
                .addParam("currentFilter", filter)  
                .render()
        );
    }
    
    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the computer to edit
     */
    @Transactional(readOnly=true)
    public static Result edit(Long id) {
        Form<Computer> computerForm = form(Computer.class).fill(
            Computer.findById(id)
        );
        return ok(
            gte.template("editForm.html")
                .addParam("id", id)
                .addParam("companyOptions", Company.options())
                .withForm(computerForm)
                .render()  
            
        );
    }
    
    /**
     * Handle the 'edit form' submission 
     *
     * @param id Id of the computer to edit
     */
    @Transactional
    public static Result update(Long id) {
        Form<Computer> computerForm = form(Computer.class).bindFromRequest();
        if(computerForm.hasErrors()) {
            return badRequest(
                gte.template("editForm.html")
                    .addParam("id", id)
                    .addParam("companyOptions", Company.options())
                    .withForm(computerForm)
                    .render()  

            );
        }
        computerForm.get().update(id);
        flash("success", "Computer " + computerForm.get().name + " has been updated");
        return GO_HOME;
    }
    
    /**
     * Display the 'new computer form'.
     */
    @Transactional(readOnly=true)
    public static Result create() {
        Form<Computer> computerForm = form(Computer.class);
        return ok(
            gte.template("createForm.html")
                .addParam("companyOptions", Company.options())
                .withForm(computerForm)
                .render()
        );
    }
    
    /**
     * Handle the 'new computer form' submission 
     */
    @Transactional
    public static Result save() {
        Form<Computer> computerForm = form(Computer.class).bindFromRequest();
        if(computerForm.hasErrors()) {
            return badRequest(
                gte.template("createForm.html")
                    .addParam("companyOptions", Company.options())
                    .withForm(computerForm)
                    .render()
            );
        }
        computerForm.get().save();
        flash("success", "Computer " + computerForm.get().name + " has been created");
        return GO_HOME;
    }
    
    /**
     * Handle computer deletion
     */
    @Transactional
    public static Result delete(Long id) {
        Computer.findById(id).delete();
        flash("success", "Computer has been deleted");
        return GO_HOME;
    }
    

}
            
