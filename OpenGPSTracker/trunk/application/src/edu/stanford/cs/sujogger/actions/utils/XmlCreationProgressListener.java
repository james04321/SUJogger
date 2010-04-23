/*------------------------------------------------------------------------------
 **     Ident: Delivery Center Java
 **    Author: rene
 ** Copyright: (c) Jan 23, 2010 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced  
 ** Distributed Software Engineering |  or transmitted in any form or by any        
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the      
 ** 4131 NJ Vianen                   |  purpose, without the express written    
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 */
package edu.stanford.cs.sujogger.actions.utils;

/**
 * Interface to monitor progress of KMZ, GPX XML creation
 * 
 * @version $Id: XmlCreationProgressListener.java 468 2010-03-28 13:47:13Z rcgroot $
 * @author rene (c) Mar 22, 2009, Sogeti B.V.
 */
public interface XmlCreationProgressListener
{
   public void startNotification( String fileName );
   public void updateNotification(int progress, int goal);
   public void endNotification( String fileName );
}