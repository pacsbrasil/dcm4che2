/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.dcm4cheri.net;

import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import org.dcm4cheri.util.LF_ThreadPool;
import org.dcm4cheri.util.IntHashtable2;

import java.io.IOException;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:umberto.cappellini@chello.at">Umberto Cappellini</a>
 * @version $Revision$ $Date$
 *   
 */
final class ActiveAssociationImpl
	implements ActiveAssociation, LF_ThreadPool.Handler, AssociationListener {
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private final AssociationImpl assoc;
	private final DcmServiceRegistry services;
	private final IntHashtable2 rspDispatcher = new IntHashtable2();
	private final IntHashtable2 cancelDispatcher = new IntHashtable2();
	private final LF_ThreadPool threadPool = new LF_ThreadPool(this);
	private boolean running = false;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	ActiveAssociationImpl(Association assoc, DcmServiceRegistry services) {
		if (assoc.getState() != Association.ASSOCIATION_ESTABLISHED)
			throw new IllegalStateException(
				"Association not established - " + assoc.getState());

		this.assoc = (AssociationImpl) assoc;
		this.services = services;
		((AssociationImpl) assoc).setThreadPool(threadPool);
		assoc.addAssociationListener(this);
	}

	// Public --------------------------------------------------------
	public final void addCancelListener(int msgID, DimseListener l) {
		cancelDispatcher.put(msgID, l);
	}
	
	public String toString() {
		return "Active-" + assoc;
	}

	public void run() {
		if (running)
			throw new IllegalStateException("Already running: " + threadPool);

		this.running = true;
		threadPool.join();
	}

	public void start() {
		if (running)
			throw new IllegalStateException("Already running: " + threadPool);

		new Thread(this).start();
	}

	public Association getAssociation() {
		return assoc;
	}

	public void invoke(Dimse rq, DimseListener l)
		throws InterruptedException, IOException {
		//      checkRunning();
		int msgID = rq.getCommand().getMessageID();
		int maxOps = assoc.getMaxOpsInvoked();
		if (maxOps == 0) {
			rspDispatcher.put(msgID, l);
		} else
			synchronized (rspDispatcher) {
				while (rspDispatcher.size() >= maxOps) {
					rspDispatcher.wait();
				}
				rspDispatcher.put(msgID, l);
			}
		assoc.write(rq);
	}

	public FutureRSP invoke(Dimse rq)
		throws InterruptedException, IOException {
		FutureRSPImpl retval = new FutureRSPImpl();
		assoc.addAssociationListener(retval);
		invoke(rq, retval);
		return retval;
	}

	public void release(boolean waitOnRSP)
		throws InterruptedException, IOException {
		//      checkRunning();
		if (waitOnRSP) {
			synchronized (rspDispatcher) {
				while (!rspDispatcher.isEmpty()) {
					rspDispatcher.wait();
				}
			}
		}
		((AssociationImpl) assoc).writeReleaseRQ();
	}

	// LF_ThreadPool.Handler implementation --------------------------
	public void run(LF_ThreadPool pool) {
		try {
			Dimse dimse = assoc.read();

			// if Association was released
			if (dimse == null) {
				pool.shutdown();
				return;
			}
			assoc.initMDC();
			Command cmd = dimse.getCommand();
			switch (cmd.getCommandField()) {
				case Command.C_STORE_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).c_store(
						this,
						dimse);
					break;
				case Command.C_GET_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).c_get(
						this,
						dimse);
					break;
				case Command.C_FIND_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).c_find(
						this,
						dimse);
					break;
				case Command.C_MOVE_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).c_move(
						this,
						dimse);
					break;
				case Command.C_ECHO_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).c_echo(
						this,
						dimse);
					break;
				case Command.N_EVENT_REPORT_RQ :
					services.lookup(
						cmd.getAffectedSOPClassUID()).n_event_report(
						this,
						dimse);
					break;
				case Command.N_GET_RQ :
					services.lookup(cmd.getRequestedSOPClassUID()).n_get(
						this,
						dimse);
					break;
				case Command.N_SET_RQ :
					services.lookup(cmd.getRequestedSOPClassUID()).n_set(
						this,
						dimse);
					break;
				case Command.N_ACTION_RQ :
					services.lookup(cmd.getRequestedSOPClassUID()).n_action(
						this,
						dimse);
					break;
				case Command.N_CREATE_RQ :
					services.lookup(cmd.getAffectedSOPClassUID()).n_create(
						this,
						dimse);
					break;
				case Command.N_DELETE_RQ :
					services.lookup(cmd.getRequestedSOPClassUID()).n_delete(
						this,
						dimse);
					break;
				case Command.C_STORE_RSP :
				case Command.C_GET_RSP :
				case Command.C_FIND_RSP :
				case Command.C_MOVE_RSP :
				case Command.C_ECHO_RSP :
				case Command.N_EVENT_REPORT_RSP :
				case Command.N_GET_RSP :
				case Command.N_SET_RSP :
				case Command.N_ACTION_RSP :
				case Command.N_CREATE_RSP :
				case Command.N_DELETE_RSP :
					handleResponse(dimse);
					break;
				case Command.C_CANCEL_RQ :
					handleCancel(dimse);
					break;
				default :
					throw new RuntimeException("Illegal Command: " + cmd);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			pool.shutdown();
		} finally {
			assoc.clearMDC();		    
		}
	}

	// Y overrides ---------------------------------------------------

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------
	private void handleResponse(Dimse dimse) throws IOException {
		DimseListener l = null;
		synchronized (rspDispatcher) {
			Command cmd = dimse.getCommand();
			Dataset ds = dimse.getDataset(); // read out dataset, if any
			int msgID = cmd.getMessageIDToBeingRespondedTo();
			if (cmd.isPending()) {
				l = (DimseListener) rspDispatcher.get(msgID);
			} else {
				l = (DimseListener) rspDispatcher.remove(msgID);
				rspDispatcher.notify();
			}
			if (l != null)
				l.dimseReceived(assoc, dimse);
		}
	}

	private void handleCancel(Dimse dimse) {
		Command cmd = dimse.getCommand();
		int msgID = cmd.getMessageIDToBeingRespondedTo();
		DimseListener l = (DimseListener) cancelDispatcher.remove(msgID);

		if (l != null)
			l.dimseReceived(assoc, dimse);
	}

	private void checkRunning() {
		if (!running)
			throw new IllegalStateException("Not running: " + threadPool);
	}

	   // AssociationListener implementation ----------------------------
	   public void write(Association src, PDU pdu) {
	   }
	   
	   public void received(Association src, Dimse dimse) {
	   }
	   
	   public void error(Association src, IOException ioe) {
	   }
	   
	   public void close(Association src) {
	       synchronized (rspDispatcher) {
		       rspDispatcher.clear();
		       rspDispatcher.notifyAll();
	       }
	       assoc.removeAssociationListener(this);
	   }
	   
	   public void write(Association src, Dimse dimse) {
	   }
	   
	   public void received(Association src, PDU pdu) {
	   }
	   
}
