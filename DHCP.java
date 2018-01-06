// DHCPP.java	  Mihnea Gheorghiu 334CC

// DHCP Protocol
package protocol;				// protocol package

import java.util.Vector;			// vector (list)
import support.*;				// protocol entity support

public class DHCP extends Protocol {		// DHCP protocol

  private DHCPSender sender;			// protocol sender (client)
  private DHCPReceiver receiver;		// protocol receiver (server)

  public DHCP () {				// construct protocol instance
    medium = new DHCPMedium ();		// construct comms medium
    sender =					// construct sender (client)
      new DHCPSender (medium, "Client");
    receiver =					// construct receiver (server)
      new DHCPReceiver (medium, "Server");
    sender.setPeer (receiver);			// sender is receiver's peer
    receiver.setPeer (sender);			// receiver is sender's peer
    entities = new Vector ();			// initialise protocol entities
    entities.addElement (sender);		// add sender protocol entity
    entities.addElement (medium);		// add comms medium entity
    entities.addElement (receiver);		// add receive protocol entity
  }

}
