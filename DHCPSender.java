// DHCPSender.java	 Mihnea Gheorghiu 334 CC
// DHCP Protocol sender (client)

package protocol;				// protocol package

import java.util.Vector;			// vector (list)
import support.*;				// protocol entity support

public class DHCPSender			// protocol sender (client)
 implements ProtocolEntity, Timeouts {		// protocol entity, timeout

  // simulator variables

  private ProtocolEntity peer;			// peer entity (client)
  private Medium medium;			// communications medium
  private String name;				// entity name

  // protocol variables

  private int xid;				// transaction identifier
  private String hwAddress = "hw";		// hardware address
  private String ipAddress = "";		// IP address
  private int flag = 0;		//flag de broadcast
  private String ciaddr = "0";		// client IP address
  private String yiaddr = "0";		// your client IP address
  private String chaddr = "AA:BB:CC:DD:EE";		// client hw address
  
 /**/private String bootFile = "boot";		// boot file name
  private PDU pduSent;				// PDU sent
  private PDU pduReceived;			// PDU received
  private boolean timerEnabled;			// whether timer is enabled

  // protocol state

  int state;					// current protocol state

 
  final static int init = 0;
  final static int selecting = 1;
  final static int requesting = 2;
  final static int initreboot = 3;
  final static int rebooting = 4;
  final static int bound = 5;
  final static int renewing = 6;
  final static int rebinding = 7;
  
  // protocol messages

  
  final static String discover = "DISCOVER";	//dhcp discover message
  final static String offer = "OFFER";	//dhcp offer message
  final static String request = "REQUEST";	//dhcp request message
  final static String ack = "ACK";	//dhcp ack message
  final static String release = "RELEASE";	//release discover message
  final static String inform = "INFORM";	//inform discover message
  // protocol methods

  public DHCPSender (Medium m, String name) {	// construct sender instance
    this.name = name;				// set protocol entity name
    medium = m;					// set underlying medium
    initialise ();				// initialise protocol
  }

  public String getName () {			// get protocol entity name
    return (name);				// return protocol entity name
  }

  public boolean hasTimer (String type) {	// protocol uses timer?
    return (true);				// report it does
  }

  public void initialise () {			// initialise protocol
    state = init;				// initialise state
    pduReceived = null;				// initialise no PDU received
    pduSent = null;				// initialise no PDU sent
    timerEnabled = false;			// initialise no timeout
  }

  public void setPeer (ProtocolEntity peer) {	// set protocol peer
    this.peer = peer;				// set this entity's peer
  }

  public void setTimer (PDU pdu, boolean b) {	// set timer status
    timerEnabled = b;				// store timer status
  }

  public Vector getServices() {
    Vector events = new Vector ();		// list of events
    String pduType;				// received PDU type
    String pduData;				// PDU data
    String pduTrans;				// transaction identifier
    int transPos, hwPos;			// trans/hw address positions
    int trans;					// transaction identifier
 	
    //stare initiala, imi generez un discover
   
    if (state == init) 
    	{		
      trans = 					// xid
        10 + ((int) (DHCPMedium.random () * 90));
     
	  flag=1;
	  ciaddr="0";
	  yiaddr="0";
	  
	  
	  events.addElement (			// send request for lease
	discover + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ") - send DHCPDISCOVER");
	  
	 }
	 
	 //starea de selecting
    if (state == selecting) {
    	if (pduReceived != null) {			// PDU received?
      pduType = pduReceived.type;		// get received PDU type
      pduData = pduReceived.sdu;		// get received PDU data
      
      if (pduType.equals (offer)) {
      timerEnabled=false;
	hwPos = pduData.indexOf (',');		// get hardware position
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	
	  yiaddr = "0";				
	   flag = 1;
	 
		
	 events.addElement (			// send request
	  request + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")- reply with request");
	  
      }
    }		
    }
    //requesting
     if (state == requesting) {
    	if (pduReceived != null) {			// PDU received?
      pduType = pduReceived.type;		// get received PDU type
      pduData = pduReceived.sdu;		// get received PDU data
        if (pduType.equals (ack)) {
        
	hwPos = pduData.indexOf (',');		// get hardware position
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	  String[] extrageIP=pduData.split(",");
	  ipAddress =extrageIP[2];
	  
	  ciaddr=ipAddress;
	  yiaddr = "0";				
	    flag = 1;
	 
	 
	 events.addElement (			// send inform
	  inform + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")- request more data");
	
      }
    }		
    }    
    	
    if (state == bound) {
    	//bound+renew+rebind din schema. las jasper sa aleaga care din cele 3 pacehte p[osibile va trimite
    	if (pduReceived != null) {			// PDU received?
      pduType = pduReceived.type;		// get received PDU type
      pduData = pduReceived.sdu;		// get received PDU data
        if (pduType.equals (ack)) {
        timerEnabled = false;
	hwPos = pduData.indexOf (',');		// get hardware position
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	  String[] extrageIP=pduData.split(",");
	  ipAddress =extrageIP[2]; //extrag ce imi tebuie
	  
	  ciaddr=ipAddress;
	  yiaddr = "0";				
	    flag = 0;
	 //expira T1
	 events.addElement (			// send request de reinnoire pe adresa veche
	  request + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")-request catre server dupa ce a expirat T1");
	  flag=1;
	 //expira T2
	 events.addElement (			
	 	// send request DHCP prin broadcast ptr a obtine o adresa noua de IP de la orice serve
	  request + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")-request  dupa ce a expirat T2");
	  
	  flag=0;
	  //expira leaseul sau sa hotarat sa iasa din retea
	 events.addElement (			// send release
	  release + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")-release");
	  flag=1;
	  
      }
    }		
    }    
    
    if (timerEnabled)
      events.addElement("Timeout - presume loss of message and resend");
    return (events);				// return list of events
  }

  public Vector performService (String s) {
    Vector events = new Vector ();		// initialise events list
    int start, middle, end;			// start/mid/end positions
    String pduData;				// PDU data

    
    if (s.startsWith (discover)) {
      start = s.indexOf ('(') + 1;		// get contents start
      middle = s.indexOf (',');			// get trans ident end
      end = s.indexOf (')' );			// get contents end
      xid =					// get transaction ident
        Integer.parseInt (s.substring (start, middle));
      pduData = s.substring (start, end);	// get SDU
      transmitPDU (				// send request
        new PDU (discover, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
      				//trece in starea urmatoare
      				state = selecting; timerEnabled=false;
    }
    if (s.startsWith (request)) {
      start = s.indexOf ('(') + 1;		// get contents start
      middle = s.indexOf (',');			// get trans ident end
      end = s.indexOf (')' );			// get contents end
      xid =					// get transaction ident
        Integer.parseInt (s.substring (start, middle));
      pduData = s.substring (start, end);	// get SDU
      transmitPDU (				// send request
        new PDU (request, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
        //vede in ce stare a fost trimis requestul ptr a seta corect starea
   	if(state!=bound)
      		state = requesting;	
   	timerEnabled=false;		
    }
    if (s.startsWith (inform)) {
      start = s.indexOf ('(') + 1;		// get contents start
      middle = s.indexOf (',');			// get trans ident end
      end = s.indexOf (')' );			// get contents end
      xid =					// get transaction ident
        Integer.parseInt (s.substring (start, middle));
      pduData = s.substring (start, end);	// get SDU
      transmitPDU (				// send request
        new PDU (inform, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
      
      state=bound;
      timerEnabled=false;
    }
    
    if (s.startsWith (release)) {
    	state = init;
      start = s.indexOf ('(') + 1;		// get contents start
      middle = s.indexOf (',');			// get trans ident end
      end = s.indexOf (')' );			// get contents end
      xid =					// get transaction ident
        Integer.parseInt (s.substring (start, middle));
      pduData = s.substring (start, end);	// get SDU
      transmitPDU (				// send request
        new PDU (release, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
      //se intoarce la starea initiala
      state = init;
      timerEnabled = false;
      
    }
    if (s.startsWith ("Timeout")) {		// timeout?
      transmitPDU (pduSent, peer);		// re-send PDU
      events.addElement (			// add timeout event and PDU
        new ProtocolEvent (ProtocolEvent.TIMEOUT, pduSent));
    }
    return (events);				// return list of events
  }

  public Vector receivePDU (PDU pdu) {		// handle received PDU
    pduReceived = pdu;				// store PDU
    return (new Vector ());			// return no events
  }

  public void transmitPDU (			// transmit PDU
   PDU pdu, ProtocolEntity dest) {		// for given PDU, destination
    pdu.setSource (this);			// source is this entity
    pdu.setDestination (dest);			// destination is as given
    pduSent = pdu;				// copy PDU sent
    medium.receivePDU (pdu);			// medium receives PDU
    pduReceived = null;				// note no PDU in response
    timerEnabled = false;			// note no timeout
  }

}
