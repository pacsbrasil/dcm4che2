package org.dcm4chee.web.war.tc.keywords;


public abstract class AbstractTCKeywordInput extends AbstractTCInput implements TCKeywordInput 
{
    public AbstractTCKeywordInput(final String id)
    {
        super(id);
    }
    
    @Override
    public Object getInputValue()
    {
        return getKeyword();
    }
    
    @Override
    public void resetInputValue()
    {
        resetKeyword();
    }
}
