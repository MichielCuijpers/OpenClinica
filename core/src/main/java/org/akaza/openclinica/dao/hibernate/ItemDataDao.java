package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.hibernate.query.Query;

public class ItemDataDao extends AbstractDomainDao<ItemData> {

    Class<ItemData> domainClass() {
        return ItemData.class;
    }



    private static String findByEventCrfGroupOrdinalQuery = "select id from ItemData id, "
            + "Item i, "
            + "ItemGroupMetadata igm, "
            + "EventCrf ec "
            + "where id.item.itemId = i.itemId "
            + "and igm.item.itemId = id.item.itemId "
            + "and id.eventCrf.eventCrfId = ec.eventCrfId "
            + "and igm.crfVersion.crfVersionId = ec.crfVersion.crfVersionId "
            + "and igm.itemGroup.itemGroupId = :itemGroupId "
            + "and id.ordinal = :ordinal "
            + "and ec.eventCrfId = :eventCrfId "
            + "and id.deleted=false ";


    private static String findByItemEventCrfOrdinalQuery =  "select id from ItemData id "
            + "join id.item i where i in :items "
            + "and id.eventCrf.eventCrfId = :eventCrfid "
            + "and id.ordinal >= 1 and id.ordinal <= :numItems "
            + "and id.deleted=false";


    private static String getMaxGroupRepeatQuery = "select max(id.ordinal) from ItemData id "
            + "join id.eventCrf ec on ec.eventCrfId = :eventCrfId "
            + "join id.item i on i.itemId = :itemId "
            + "and id.deleted = false";

    public List<ItemData> findByItemsEventCrf(List<Item> items, Integer eventCrfId, Integer numItems) {
        Query q = getCurrentSession().createQuery(findByItemEventCrfOrdinalQuery);
        q.setParameter("items", items);
        q.setParameter("eventCrfid", eventCrfId);
        q.setParameter("numItems", numItems);
        return (List<ItemData>) q.list();
    }

    public ItemData findByItemEventCrfOrdinal(Integer itemId, Integer eventCrfId, Integer ordinal) {
        String query = "from " + getDomainClassName()
                + " item_data where item_data.item.itemId = :itemid and item_data.eventCrf.eventCrfId = :eventcrfid and item_data.ordinal = :ordinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemid", itemId);
        q.setInteger("eventcrfid", eventCrfId);
        q.setInteger("ordinal", ordinal);
        return (ItemData) q.uniqueResult();
    }

    public List<ItemData> findByEventCrfGroup(Integer eventCrfId, Integer itemGroupId) {
        String query = "select id.* " + 
            "from item_data id " + 
            "join item i on id.item_id = i.item_id " + 
            "join event_crf ec on id.event_crf_id=ec.event_crf_id " + 
            "join item_group_metadata igm on i.item_id=igm.item_id and igm.crf_version_id = ec.crf_version_id " + 
            "where id.event_crf_id = " + eventCrfId + " and igm.item_group_id = " + itemGroupId + " " + 
            "order by id.ordinal, igm.ordinal";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemData.class);
        
        return (List<ItemData>) q.list();
      
    }


    public ItemData findByEventCrfGroupOrdinal(EventCrf eventCrf, Integer itemGroupId, Integer ordinal) {
        Query q = getCurrentSession().createQuery(findByEventCrfGroupOrdinalQuery);
        q.setParameter("eventCrfId", eventCrf.getEventCrfId());
        q.setParameter("itemGroupId", itemGroupId);
        q.setParameter("ordinal", ordinal);
        return (ItemData) q.uniqueResult();
    }

    public List<ItemData> findByEventCrfId(Integer eventCrfId) {
        String query = "from " + getDomainClassName() + " item_data where item_data.eventCrf.eventCrfId = :eventcrfid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventcrfid", eventCrfId);
        return (List<ItemData>) q.list();
      
    }
    
    public int getMaxGroupRepeat(Integer eventCrfId, Integer itemId) {
        Query q = getCurrentSession().createQuery(getMaxGroupRepeatQuery);
        q.setParameter("eventCrfId", eventCrfId);
        q.setParameter("itemId", itemId);
        Number result = (Number) q.uniqueResult();
        if (result == null) return 0;
        else return result.intValue();
    }
}
