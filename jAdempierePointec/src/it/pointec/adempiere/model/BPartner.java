package it.pointec.adempiere.model;

public class BPartner {

	private String taxcode;
	private String vatnumber;
	private String firstname;
	private String lastname;
	private String company;
	private String street;
	private String postcode;
	private String city;
	private String region_code;
	private String telephone;
	private String email;
	private String customer_id;
	private int is_business_address;
	
	public String getTaxcode() {
		
		String s = taxcode.replace(" ", "");
		
		if (ControllaCF(s))
			return s;
		else {
			if (ControlloPIVA(s))
				return s;
			else
				return null;
		}
	}
	public String getTaxcode2() {
		return taxcode.replace(" ", "");
	}
	public String getVatnumber() {
		if (ControlloPIVA(vatnumber))
			return vatnumber;
		else {
			if (ControlloPIVA(taxcode))
				return taxcode;
			else
				return null;
		}
	}
	public String getFirstname() {
		return firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public String getCompany() {
		return company;
	}
	public String getStreet() {
		return street;
	}
	public String getPostcode() {
		return postcode;
	}
	public String getCity() {
		return city;
	}
	public String getRegion_code() {
		return region_code;
	}
	public String getTelephone() {
		return telephone;
	}
	public String getEmail() {
		return email;
	}
	public String getCustomer_id() {
		return customer_id;
	}
	
	public int getIs_business_address() {
		return is_business_address;
	}
	
	public String getValue() {
		
		// Verifica se la partita del BPartner è corretta
		// Se corretta diventa la chiave del BPartner
		if (ControlloPIVA(vatnumber))
			return vatnumber;
				
		// Verifica se il codice fiscale del BPartner è corretto
		// Se corretto diventa la chiave del BPartner
		if (ControllaCF(taxcode))
			return taxcode;
		
		return "ID_" + getCustomer_id();
		
	}
	
	/**
	 * Controllo del codice fiscale
	 * @param cf
	 * @return
	 */
	private boolean ControllaCF(String cf) {
        int i, s, c;
        String cf2;
        int setdisp[] = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
            11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23 };
        if( cf.length() == 0 ) return false;
        if( cf.length() != 16 )
            return false;
        cf2 = cf.toUpperCase();
        for( i=0; i<16; i++ ){
            c = cf2.charAt(i);
            if( ! ( c>='0' && c<='9' || c>='A' && c<='Z' ) )
                return false;
        }
        s = 0;
        for( i=1; i<=13; i+=2 ){
            c = cf2.charAt(i);
            if( c>='0' && c<='9' )
                s = s + c - '0';
            else
                s = s + c - 'A';
        }
        for( i=0; i<=14; i+=2 ){
            c = cf2.charAt(i);
            if( c>='0' && c<='9' )     c = c - '0' + 'A';
            s = s + setdisp[c - 'A'];
        }
        if( s%26 + 'A' != cf2.charAt(15) )
            return false;
        return true;
    }
	
	/**
	 * Controllo partita iva
	 * @param pi
	 * @return
	 */
	private boolean ControlloPIVA(String pi){
    	
		int i, c, s;
	    if( pi.length() == 0 )  return false;
	    if( pi.length() != 11 ) return false;
	    
	    for( i=0; i<11; i++ ){
	        if( pi.charAt(i) < '0' || pi.charAt(i) > '9' )
	            return false;
	    }
	    s = 0;
	    for( i=0; i<=9; i+=2 )
	        s += pi.charAt(i) - '0';
	    for( i=1; i<=9; i+=2 ){
	        c = 2*( pi.charAt(i) - '0' );
	        if( c > 9 )  c = c - 9;
	        s += c;
	    }
	    if( ( 10 - s%10 )%10 != pi.charAt(10) - '0' )
	        return false;
	    
	    return true;
	}
	
}
