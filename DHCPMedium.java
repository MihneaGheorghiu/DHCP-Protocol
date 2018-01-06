// DHCPMedium.java	 Mihnea Gheorghiu 334CC

// DHCP medium

package protocol;				// protocol package

import java.util.*;				// utility support
import support.*;				// protocol entity support

public class DHCPMedium extends Medium {	// protocol medium

  // protocol variables

  private static Vector randoms;		// random number list
  private static int randomIndex;		// random number index

  public DHCPMedium() {			// construct medium instance
    super();					// construct as generic medium
    randoms = new Vector();			// initialise list of randoms
  }

  protected PDU getMatchingPDU (String s) {	// get matching PDU on channel
    PDU pdu;					// PDU
    String sdu;					// PDU contents

    int sourceStart = s.indexOf ('[') + 1;	// get start of entity name
    int sourceEnd = s.indexOf (']');		// get end of entity name
    String sourceName =				// get PDU source
      s.substring (sourceStart, sourceEnd);
    int typeStart = s.indexOf (' ') + 1;	// get start of PDU type
    int typeEnd = s.indexOf ('(');		// get end of PDU type
    String type =				// get PDU type
      s.substring (typeStart, typeEnd);
    int parEnd = s.indexOf (')');		// get end of PDU parameters
    sdu = s.substring (typeEnd + 1, parEnd);	// get PDU parameters
    for (Enumeration e = pdus.elements();	// go through PDUs on channel
         e.hasMoreElements(); ) {
      pdu = (PDU) e.nextElement ();		// get next PDU on channel
      if (pdu != null &&			// valid PDU and ...
          pdu.type.equals(type) &&		// type matches and ...
          pdu.getSource().getName(). 		// source matches and ...
	    equals(sourceName) &&
          sdu.equals (pdu.sdu))			// SDU matches
        return (pdu);				// return with this PDU
    }
    return (null);				// return no PDU as no match
  }

  public void initialise () {			// initialise medium
    super.initialise ();			// initialise generic medium
    randomIndex = 0;				// initialise randoms index
  }

  protected static float random () {		// random number (from list)
    Float rand;					// random number

    if (randomIndex < randoms.size ())		// get number from list?
      rand = (Float) randoms.elementAt (randomIndex++); // get number from list
    else {					// make new random number
      rand = new Float (Math.random ());	// get random number
      randoms.addElement (rand);		// add to list
      randomIndex++;				// increment list index
    }
    return (rand.floatValue ());		// return random number
  }

}
