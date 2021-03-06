package com.platonic;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.platonic.beans.ProfileBean;

@Path("profile")
public class Profile {
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ProfileBean getProfile(@FormParam("id") int id)
	{
		String qString = "SELECT ?p ?o ?label" +
"WHERE" +
"{" +
	"<http://www.platonic.com#"+id+"> ?p ?o." +
     "   OPTIONAL{?o <http://xmlns.com/foaf/0.1/name> ?label}." +
	     "OPTIONAL{?o <http://www.w3.org/2004/02/skos/core#prefLabel> ?label}." +
"}";
		

		ResultSet rs = runQuery(qString);
		
		ProfileBean profile = new ProfileBean();
		profile.setId(id);
		ArrayList<String> affiliations = new ArrayList<String>();
		ArrayList<String> interests = new ArrayList<String>();
		while (rs.hasNext())
		{
			QuerySolution qs = rs.next();
			if (qs.getResource("?p").getLocalName().equals("name"))
				profile.setName(qs.getLiteral("?o").getString());
			if (qs.getResource("?p").getLocalName().equals("affiliation"))
			{
				affiliations.add(qs.getResource("?o").getLocalName());
				if (qs.getLiteral("?label") != null)
					affiliations.add(qs.getLiteral("?label").getString());
				
			}
			if(qs.getResource("?p").getLocalName().equals("interest"))
			{
				
				if (qs.getLiteral("?label") != null)
					interests.add(qs.getLiteral("?label").getString());
			}
			
			profile.setAffiliation(affiliations);
			profile.setInterests(interests);
			
			
		}
			
		
		return profile;
	}

	public ResultSet runQuery(String qString) {
		Dataset ds = TDBFactory.createDataset("ds");
		ds.begin(ReadWrite.READ );
		com.hp.hpl.jena.query.Query q = QueryFactory.create(qString);
		QueryExecution exec = QueryExecutionFactory.create(q, ds.getDefaultModel());
		ResultSet rs = exec.execSelect(); 
		ds.commit();
		ds.close();
		return rs;
	}
	
	
}
