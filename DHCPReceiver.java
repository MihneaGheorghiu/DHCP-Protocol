// DHCPReceiver.java	 Mihnea Gheorghiu 334CC
// DHCP Protocol receiver (server)

package protocol;				// protocol package

import java.util.Vector; // vector (list)
import java.util.Random;			
import support.*;				// protocol entity support

public class DHCPReceiver			// protocol receiver (server)
 implements ProtocolEntity {			// protocol entity

  // simulator variables

  private ProtocolEntity peer;			// peer entity (client)
  private Medium medium;			// communications medium
  private String name;				// entity name

  // protocol variables

   private String leased;
   private int xid;				// transaction identifier
  private String hwAddress = "";		// hardware address
  private int flag = 0;		//flag de broadcast
  private String ciaddr = "0";		// client IP address
  private String yiaddr = "0";		// your client IP address
  private String chaddr = "AA:BB:CC:DD:EE";		// client hw address
  private String ipAddress = "";		// IP address root
  private PDU pduSent;				// PDU sent
  private PDU pduReceived;			// PDU received
  

  // protocol messages

 // final static String request = "REQUEST";	// boot request message
 /**/final static String reply = "REPLY";		// boot reply message
  final static String discover = "DISCOVER";	//dhcp discover message
  final static String offer = "OFFER";	//dhcp offer message
  final static String request = "REQUEST";	//dhcp request message
  final static String ack = "ACK";	//dhcp ack message
  final static String release = "RELEASE";	//release discover message
  final static String inform = "INFORM";	//inform discover message


  public DHCPReceiver (Medium m, String name) { // construct receiver instance
    this.name = name;				// set protocol entity name
    medium = m;					// set underlying medium
    initialise ();				// initialise protocol
  }

  public String getName () {			// get protocol entity name
    return (name);				// return protocol entity name
  }

  public void initialise () {			// initialise protocol
    pduReceived = null;				// initialise no PDU received
    pduSent = null;				// initialise no PDU sent
  }

  public Vector getServices() {
    Vector events = new Vector ();		// initialise events list
    String pduType;				// received PDU type
    String pduData;				// PDU data
    String pduTrans;				// transaction identifier
    //String[] extractor = pduData.split(",");
    int hwPos, ipPos, bootPos;			// hw/ip/boot file positions
    int trans;					// transaction identifier
    String hw, ip;				// hardware/IP addr address
    String boot;				// boot file name

    if (pduReceived != null) {			// PDU received?
      pduType = pduReceived.type;		// get received PDU type
      pduData = pduReceived.sdu;		// get received PDU data
      //primeste discover
      if (pduType.equals (discover)) {
	hwPos = pduData.indexOf (',');		// get hardware position
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	String[] extractor = pduData.split(",");
	hwAddress=extractor[3];
	  long timpstart = System.currentTimeMillis();
	  long rand = timpstart%19;
	  //daca nu ia dat adresa ii da 
	  //evit bugul in care primeste 2 offeruri si eventual se mai si suprascrie adresa
	  //de ip pe care io daduse initial
	  //serverul oricum are nevoie de o mapare ca sa stie ce a dat..asta e varainta ptr un server un host unde nu tre
	  //sa retin 
	  Random r = new Random();
	  if(ipAddress == "")
	  yiaddr =					// random IP address 10..99
	    "192.168.5." + (rand + (int) (DHCPMedium.random () * 99))+"\\24";
	    flag = 1;
	    ipAddress = yiaddr;
	 
	      
	 events.addElement (			// send offer
	  offer + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")- reply with IP address and time");
      }
   
   
     		//raspunde la request
      if (pduType.equals (request)) {
	hwPos = pduData.indexOf (',');	
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	  long timpstart = System.currentTimeMillis();
	  long t1=3000;
	  
	  String[] extractor = pduData.split(",");
	  if(extractor[4].equals("1") && (extractor[1].equals("0")==false) ) {
	  //vad stare si flagul ptr a stii cum sa raspund
	  	yiaddr =					
	    	"192.168.5." + (5 + (int) (DHCPMedium.random () * 99))+"\\24";
	    	ipAddress = yiaddr;
	  }
	 else yiaddr = ipAddress;
	 flag =new Integer(extractor[4]);		//pastrez flagul	
	 
	 events.addElement (			// send ACK
	  ack + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")- ACK");
      }
      //reactioneaza la inform
        if (pduType.equals (inform)) {
	hwPos = pduData.indexOf (',');		// cauta virgula
	trans =					// get transaction identifier
	  Integer.parseInt (pduData.substring (0, hwPos));
	  
	  
	yiaddr = ipAddress;			
	 
	 events.addElement (			// send ACK
	  ack + "(" + trans + "," +
	  ciaddr +","+ yiaddr +","+ chaddr + "," + flag + ")- ACK");
      }
      if (pduType.equals (release)) {

	    	ipAddress ="";
	  }
	 
      
    }
    return (events);
  }
//trimiterea 
  public Vector performService(String s) {
    Vector events = new Vector ();		// initialise events list
    int start, middle, end;			// start/middle/end positions
    String pduData;				// PDU data

    if (s.startsWith (offer)) {			// reply?
      start = s.indexOf ('(') + 1;		// get contents start
      end = s.indexOf (')');			// get contents end
      pduData = s.substring (start, end);	// get data contents
      transmitPDU (				// send reply
        new PDU (offer, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
    }
    
    if (s.startsWith (ack)) {			// reply?
      start = s.indexOf ('(') + 1;		// get contents start
      end = s.indexOf (')');			// get contents end
      pduData = s.substring (start, end);	// get data contents
      transmitPDU (				// send reply
        new PDU (ack, pduData), peer);
      events.addElement (			// transmit PDU
        new ProtocolEvent (ProtocolEvent.TRANSMIT, pduSent));
    }
    return (events);
  }

  public Vector receivePDU (PDU pdu) {		// handle received PDU
    pduReceived = pdu;				// store PDU
    return (new Vector ());			// return no events
  }

  public void setPeer (ProtocolEntity peer) {	// set protocol peer
    this.peer = peer;				// set this entity's peer
  }

  public void transmitPDU (			// transmit PDU
   PDU pdu, ProtocolEntity dest) {		// for given PDU, destination
    pdu.setSource (this);			// source is this entity
    pdu.setDestination (dest);			// destination is as given
    pduSent = pdu;				// copy PDU sent
    medium.receivePDU (pdu);			// medium receives PDU
    pduReceived = null;				// note no PDU in response
  }

}
