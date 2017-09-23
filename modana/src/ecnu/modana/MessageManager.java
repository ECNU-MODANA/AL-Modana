package ecnu.modana;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ecnu.modana.abstraction.IPlugin;
import ecnu.modana.base.PluginMessage;
import ecnu.modana.util.XMLHandler;

/**
 * manage plugin message (including sendMsg, loadMsgs, checkMsgDependency)
 * @author cb
 */
class MessageManager {

	private Logger logger = Logger.getRootLogger();
	
	/**
	 * hold a plugin manager reference
	 */
	private PluginManager pluginManager;
	
	private HashMap<String, PluginMessage> msgMap;

	public MessageManager(PluginManager pluginManager) {
		super();
		msgMap = new HashMap<String, PluginMessage>();
		this.pluginManager = pluginManager;
	}
	
	/**
	 * load all registered messages from plugin-cfg.xml
	 * @return whether successfully loading
	 */
	public boolean loadMsgs() {
		boolean b = true;
		try {
        	//read xml path
    		String path = Modana.getModanaProperty(ModanaProperty.pluginCfgFile);
    		//read xsd path
    		String xsdPath = Modana.getModanaProperty(ModanaProperty.pluginCfgXsdFile);
    		
			XMLHandler handler = new XMLHandler(path);
			//validate xml file
			b = handler.validateByXSD(xsdPath);
			if (b) { //read xml file if it is valid
				SAXReader reader = new SAXReader();
				Document doc = reader.read(path);
				Element rootElement = doc.getRootElement();
				 //get all messages
				Element pmElement = rootElement.element("plugin-message");
				Iterator<?> it = pmElement.elementIterator("msg");
				while (it.hasNext()) {
					Element msgElem = (Element) it.next();
					String name = msgElem.attributeValue("name"); //get msg name
					
					if (msgMap.get(name) == null) { // first check if there exists duplicate message in msgMap
						String receiver = msgElem.element("receiver-class").getTextTrim(); //get msg recevier
						Element dataTypeElem = (Element) msgElem.element("data-types");
						if (dataTypeElem != null) {
							ArrayList<String> dataTypes = new ArrayList<String>();
							Iterator<?> i = dataTypeElem.elementIterator("type");
							while (i.hasNext()) {
								dataTypes.add(((Element) i.next()).getTextTrim()); //get each data-type
							}
							msgMap.put(name, new PluginMessage(receiver, name, 
									dataTypes.toArray(new String[1]) ) ); // add msg to map
						} else {
							msgMap.put(name, new PluginMessage(receiver, name, null));
						}
					} else {
							logger.warn("There exist duplicate messages!");
						b = false;
					}
					
				}
			}
		}  catch (DocumentException e) {
			logger.error("Read document error!", e);
			b = false;
		} catch (Exception e) {
			logger.error("Unknown error!", e);
			b = false;
		}
		return b;
	}
	
	/**
	 * check if the receiver class of each message exists
	 * @return checking result
	 */
	public boolean checkMsgDependency() {
		boolean result = true;
		Iterator<PluginMessage> it = msgMap.values().iterator();
		while (it.hasNext()) {
			PluginMessage pm = (PluginMessage) it.next();
			Class<IPlugin> cls = pluginManager.getSpecifiedPlugin(pm.getReceiverClass());
			if (null == cls) {
				logger.error("receiverClass (" + pm.getReceiverClass() + ") is not found!");
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * manually register new message and check its receiver dependency
	 * @param msgName new message name
	 * @param receiverName receiver class name of the message
	 * @param datatypes transferred data types
	 */
	public void addNewMsg(String msgName, String receiverName, String[] datatypes) {
		if (msgMap.get(msgName) == null) {
			msgMap.put(msgName, new PluginMessage(receiverName, msgName, 
					datatypes)); // add msg to map
			Class<IPlugin> cls = pluginManager.getSpecifiedPlugin(receiverName);
			if (null == cls) {
				logger.error("receiverClass (" + receiverName + ") is not found!");
			}
		} else {
			logger.warn("There exist duplicate messages!");
		}
	}
	
	/**
	 * get message by name
	 * @param msgName message name
	 * @return message object
	 */
	public PluginMessage getMessageByName(String msgName) {
		return msgMap.get(msgName);
	}
	
	/**
	 * get all plugin messages
	 * @return Collection of messages
	 */
	public Collection<PluginMessage> getAllMessages() {
		return msgMap.values();
	}
	
}
