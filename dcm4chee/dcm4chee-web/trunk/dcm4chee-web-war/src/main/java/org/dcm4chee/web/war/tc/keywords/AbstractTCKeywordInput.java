package org.dcm4chee.web.war.tc.keywords;

import org.dcm4chee.web.dao.tc.TCQueryFilterKey;

public abstract class AbstractTCKeywordInput extends AbstractTCInput implements TCKeywordInput 
{
	private static final long serialVersionUID = 8282801535686632421L;

	public AbstractTCKeywordInput(final String id, TCQueryFilterKey filterKey, boolean usedForSearch)
    {
        super(id, filterKey, usedForSearch);
    }
	
	@Override
	public TCKeyword getKeyword()
	{
		TCKeyword[] keywords = getKeywords();
		return keywords!=null && keywords.length>0 ? keywords[0] : null;
	}
	
	@Override
	public TCKeyword getValue()
	{
		return getKeyword();
	}
    
    @Override
    public TCKeyword[] getValues()
    {
        return getKeywords();
    }
    
    @Override
    public void resetValues()
    {
        resetKeywords();
    }
}
