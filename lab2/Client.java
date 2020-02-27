/**
 * java client <mcast_addr> <mcast_port> <oper> <opnd> *
 *
 * <mcast_addr>
 *      is the IP address of the multicast group used by the server to advertise its service;
 * <mcast_port>
 *      is the port number of the multicast group used by the server to advertise its service;
 * <oper>
 *      is ''register'' or ''lookup'', depending on the operation to invoke;
 * <opnd>*
 *      is the list of operands of the specified operation:
 *          <DNS name> <IP address>, for register;
 *          <IP address>, for lookup.
 */
