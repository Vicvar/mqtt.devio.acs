/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 *
 *    Created on Sep 9, 2003
 *
 */
 
  
// $Author: mbauhofe $
// $Date: 2011/06/14 14:02:55 $
// $Log: IdentifierManager.java,v $
// Revision 1.20  2011/06/14 14:02:55  mbauhofe
// Replaced XML_HISTORY concept with UID_LOOKUP (view).
// Some clean up/core reorganization.
//
// Revision 1.19  2009/11/06 12:53:49  hmeuss
// Fixed bug that "uid://" part was missing for Range document UIDs
//
// Revision 1.18  2009/06/23 13:58:13  hmeuss
// removed unused variable
//
// Revision 1.17  2009/06/19 08:50:32  hmeuss
// archiveId may now be an arbitrary string.
//
// Revision 1.16  2007/07/12 13:14:08  hmeuss
// Changed ID storage in Oracle: Now sequences are used
//
// Revision 1.15  2007/07/12 13:06:51  hmeuss
// Changed ID storage in Oracle: Now sequences are used
//
// Revision 1.14  2006/10/26 15:34:41  hmeuss
// Added check, whether archive ID of incoming documents matches archive ID
//
// Revision 1.13  2006/10/26 13:49:24  hmeuss
// ArchiveId is 0 as default from now on.
//
// Revision 1.12  2006/10/24 15:09:45  hmeuss
// getIdNamespace() returns new style UIDs now.
//
// Revision 1.11  2006/09/14 14:14:33  hsommer
// IdentifierRange.serialized was renamed to isLocked
//
// Revision 1.10  2006/04/12 09:07:32  hmeuss
// First version using the new UID scheme
//
// Revision 1.9  2005/10/19 12:16:59  sfarrow
// added the initialize method, must be called by all of the subclasses on creation
//
// Revision 1.8  2005/09/29 14:19:16  sfarrow
// sorted out starage of identifiers
//
// Revision 1.7  2005/09/20 13:43:39  sfarrow
// Forgot to increment the range id, schoolboy error :-(
//
// Revision 1.6  2005/09/14 15:58:12  sfarrow
// Altered the operation of the Identifier manager, uses an alternative architecture
//
// Revision 1.5  2004/09/23 11:59:15  hmeuss
// Oracle DatabaseReader now creates a new connection object in every method call.
//
// Added ModuleCriticalException to more methods of the internal IF
//
// Revision 1.4  2003/10/07 20:14:44  sfarrow
// *** empty log message ***
//
// Revision 1.3  2003/10/07 16:20:05  sfarrow
// *** empty log message ***
//
// Revision 1.2  2003/09/19 09:53:34  sfarrow
// Alterations to the Interface
//
// Revision 1.1  2003/09/09 08:51:30  hmeuss
// *** empty log message ***
// 
 
package alma.archive.database.interfaces;

import java.net.URI;

import alma.ArchiveIdentifierError.wrappers.AcsJRangeExhaustedEx;
import alma.acs.container.archive.Range;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.range.IdentifierRange;
import alma.archive.range.IdentifierRangeEntityT;
import alma.archive.range.RangeT;
import alma.archive.wrappers.ArchiveTimeStamp;

/**
 * @author hmeuss
 *
 */
public abstract class IdentifierManager
{    
    /** 
     * Used temporarily in order to support getRangeId()
     */
    private Range range = null;
    
    /**
     * 
     * Temporarily returns a UID from the internal range
     * 
     * DEPRECATED
     * Returns an old-style UID with a unique glocbal namespce, included for
     * backwards compatability. Uses the abstract methods to store any changed
     * values back in the database.
     * @return
     * @throws ArchiveException
     * @throws DatabaseException
     * @throws ModuleCriticalException
     */
    public URI getIdNamespace() throws ArchiveException, DatabaseException, 
            ModuleCriticalException {
        // System.out.println("getIdNamespace used!!!! This should not occur!!!");
        
        if (range==null) {
            range = new Range(getNewRange());
        }
        try {
            return range.next();    
        } catch (AcsJRangeExhaustedEx e) {
            // fetch a new range and try the same again:
            range=null;
            return getIdNamespace();
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }
    }
    
    /**
     * Returns a range castor class created from the Archive ID and the latest
     * rage id. Uses the abstract methods to store any changed values in the
     * database.
     * @return
     * @throws ArchiveException
     * @throws DatabaseException
     * @throws ModuleCriticalException
     */
    public synchronized IdentifierRange getNewRange() 
            throws ArchiveException, DatabaseException, 
            ModuleCriticalException {
        String archiveid = this.getArchiveId();
        long rangeid = this.getRangeId();
        
        //Create the entity information
        IdentifierRangeEntityT entityt = new IdentifierRangeEntityT();
        //The id of the range is the 0 document id in that range.
        entityt.setEntityId(createUid(archiveid,rangeid));

        IdentifierRange range = new IdentifierRange();
        range.setIdentifierRangeEntity(entityt);
        
        //set the time stamp
        ArchiveTimeStamp ts = new ArchiveTimeStamp();
        range.setCreatedTimeStamp(ts.toISOString());
        
        range.setIsLocked(false);
        
        range.setArchiveID(archiveid);
        
        RangeT ranget = new RangeT();
        ranget.setRangeID(Long.toHexString(rangeid));
        ranget.setBaseDocumentID("1");
        range.setRange(ranget);

        // special case for exist, in case of Oracle nothing is done
        rangeid++;
        this.setRangeId(rangeid);
        
        return range;
    }
    
    private String createUid(String archiveid, long rangeid)
    {
        String uid = "uid://"+archiveid+
            "/X" + Long.toHexString(rangeid) +"/X0";
        return uid;
    }
    
    /**
     * Return a restricted range castor class, Uses the abstract methods to
     * store any changed values in the database.
     * @param size
     * @return
     * @throws ArchiveException
     * @throws DatabaseException
     * @throws ModuleCriticalException
     */
    public synchronized IdentifierRange getNewRange(int size) 
            throws ArchiveException, DatabaseException, 
            ModuleCriticalException {
        IdentifierRange range = this.getNewRange();
        RangeT ranget = range.getRange();
        ranget.setMaxDocumentID(Integer.toHexString(size));
        return range;
    }
    
    /**
     * This will eventually configure the Archve ID and perform any start of day
     * setup. For the time being it just sets the Archive ID to 1.
     * @throws DatabaseException
     */
    protected abstract void initialize() 
            throws DatabaseException, ModuleCriticalException;
    
    /**
     * Free up any resources.
     * @throws DatabaseException
     */
    public abstract void close() 
            throws DatabaseException;
    
    /**
     * Return the Archive ID value stored in the Database, if no value is set
     * return 0 not null. It is vital that an initial value of 0 is returned.
     * @return
     * @throws DatabaseException
     */
    public abstract String getArchiveId() 
            throws DatabaseException, ModuleCriticalException;
    
    /**
     * Store the Archive ID in the Database, calling set will overwrite any
     * previous value. 
     * @param archiveId
     * @throws DatabaseException
     */
    protected abstract void setArchiveId(String archiveId) 
            throws DatabaseException, ModuleCriticalException;
    
    /**
     * Return the Range ID value stored in the Database, if no value is set
     * return 0 not null;
     * @return
     * @throws DatabaseException
     */
    protected abstract long getRangeId() 
            throws DatabaseException, ModuleCriticalException;
    
    /**
     * Store the Range ID in the database, calling set will overwrite any 
     * previous value.
     * @param rangeId
     * @throws DatabaseException
     */
    protected abstract void setRangeId(long rangeId) 
            throws DatabaseException, ModuleCriticalException;
}
