package github.madmarty.madsonic.domain;

	public enum MadsonicTheme {
		
    DARK  		 (1),
    LIGHT 		 (2),
    HOLO  		 (3),
    RED   		 (4),
    PINK    	 (5),
    GREEN    	 (6),
    EMERALD		 (7),
    BLACK		 (8),
    
    DARK_FULL  	 (9),
    LIGHT_FULL   (10),
    HOLO_FULL 	 (11),
    RED_FULL 	 (12),
    PINK_FULL 	 (13),
    GREEN_FULL   (14),
    EMERALD_FULL (15),
    BLACK_FULL   (16);
	    
	private int code; 
	
	private MadsonicTheme(int c){
		code = c;
	}
	
	public int getThemeCode() {
		return code;
	}
}
