package com.rafkind.paintown.animator;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import org.swixml.SwingEngine;

import com.rafkind.paintown.Token;
import com.rafkind.paintown.RelativeFileChooser;
import com.rafkind.paintown.animator.DrawArea;
import com.rafkind.paintown.animator.SpecialPanel;
import com.rafkind.paintown.animator.Animator;

public class CharacterAnimation
{
	// Animator
	private Animator _animator;
	
	private DrawArea area;
	private SwingEngine animEditor;
	private SwingEngine contextEditor;
	private JPanel context;
	private JPanel canvas;
	private JPanel other;
	private JTextField nameField;
	private JComboBox typeCombo;
	private JList keyList;
	private JComboBox keySelect;
	private JButton keyAdd;
	private JButton keyRemove;
	private JButton keyUp;
	private JButton keyDown;
	private JSpinner rangeSpinner;
	private JTextField basedirField;
	private JButton basedirButton;
	private JList eventList;
	private JButton eventAdd;
	private JButton eventRemove;
	private JButton eventUp;
	private JButton eventDown;
	
	// Name
	private String name = "";
	
	// Type if special
	private String type = "";
	
	// key sequence (String)
	private Vector keys = new Vector();
	
	// Range
	private int range;
	
	// Face
	private String face = "";
	
	// Base dir
	private String baseDirectory = "";
	
	// Events
	private Vector events = new Vector();
	
	public void setName(String n)
	{
		name = n;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setType(String t)
	{
		type = t;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void addKey(String key)
	{
		keys.addElement(key);
	}
	
	public Vector getKeys()
	{
		return keys;
	}
	
	public void clearKeys()
	{
		keys.clear();
	}
	
	public void setFace(String f)
	{
		face = f;
	}
	
	public String getFace()
	{
		return face;
	}
	
	public void setRange(int r)
	{
		range = r;
	}
	
	public int getRange()
	{
		return range;
	}
	
	public void setBaseDirectory(String b)
	{
		baseDirectory = b;
	}
	
	public String getBaseDirectory()
	{
		return baseDirectory;
	}
	
	public void addEvent(String[] event)
	{
		events.addElement(event);
	}
	
	public void removeEvent(String[] event)
	{
		events.removeElement(event);
	}
	
	public Vector getEvents()
	{
		return events;
	}
	
	public Token getToken()
	{
		Token token = new Token();
		token.addToken( new Token( "anim" ) );
		
		token.addToken(new String[]{"name", "\"" + name + "\""});
		if(!type.equals("none"))token.addToken(new String[]{"type", type});
		if(!keys.isEmpty())
		{
			Token keyToken = new Token( "keys" );
			keyToken.addToken( new Token( "keys"));
			Iterator kItor = keys.iterator();
			while(kItor.hasNext())
			{
				String key = (String)kItor.next();
				keyToken.addToken(new Token(key));
			}
			token.addToken(keyToken);
		}
		if(range!=0)token.addToken(new String[]{"range", Integer.toString(range)});
		if(!baseDirectory.equals(""))token.addToken(new String[]{"basedir", baseDirectory});
		Iterator fItor = events.iterator();
		while(fItor.hasNext())
		{
			String[] args = (String[])fItor.next();
			token.addToken(args);
		}
		
		return token;
	}
	
	public SpecialPanel getEditor()
	{	
		return new SpecialPanel((JPanel)animEditor.getRootComponent(),nameField);
	}
	
	public DrawArea getDrawArea()
	{
		return area;
	}
	
	public CharacterAnimation(Animator anim)
	{
		_animator = anim;
		
		name = "New Animation";
		
		type = "none";
		
		animEditor = new SwingEngine( "animator/base.xml" );
		
		contextEditor = new SwingEngine ( "animator/animation.xml");
		
		context = (JPanel) animEditor.find( "context" );
		
		nameField = (JTextField) contextEditor.find( "name" );
		
		nameField.setText(name);
		
		nameField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				name = nameField.getText();
			}
			public void insertUpdate(DocumentEvent e)
			{
				name = nameField.getText();
			}
			public void removeUpdate(DocumentEvent e)
			{
				name = nameField.getText();
			}
		});
		
		typeCombo = (JComboBox) contextEditor.find( "type" );
		typeCombo.addItem(new String("none"));
		typeCombo.addItem(new String("attack"));
		typeCombo.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				type = (String)typeCombo.getSelectedItem();
			}
		});
		
		keyList = (JList) contextEditor.find( "keys");
		keySelect = (JComboBox) contextEditor.find( "key-select" );
		keySelect.addItem(new String("key_idle"));
		keySelect.addItem(new String("key_up"));
		keySelect.addItem(new String("key_down"));
		keySelect.addItem(new String("key_back"));
		keySelect.addItem(new String("key_forward"));
		keySelect.addItem(new String("key_upperback"));
		keySelect.addItem(new String("key_upperforward"));
		keySelect.addItem(new String("key_downback"));
		keySelect.addItem(new String("key_downforward"));
		keySelect.addItem(new String("key_jump"));
		keySelect.addItem(new String("key_block"));
		keySelect.addItem(new String("key_attack1"));
		keySelect.addItem(new String("key_attack2"));
		keySelect.addItem(new String("key_attack3"));
		keySelect.addItem(new String("key_attack4"));
		keySelect.addItem(new String("key_attack5"));
		keySelect.addItem(new String("key_attack6"));
		keyAdd = (JButton) contextEditor.find( "add-key" );
		keyAdd.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				keys.addElement((String)keySelect.getSelectedItem());
				keyList.setListData(keys);
			}
		});
		keyRemove = (JButton) contextEditor.find( "remove-key" );
		keyRemove.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				if(keys.isEmpty()==true)return;
				String temp = (String)keys.elementAt(keyList.getSelectedIndex());
				keys.removeElement(temp);
				keyList.setListData(keys);
			}
		});
		keyUp = (JButton) contextEditor.find( "up-key" );
		keyUp.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				if(keys.isEmpty()==true)return;
				int index1 = keyList.getSelectedIndex()-1 < 0 ? 0 : keyList.getSelectedIndex() - 1;
				int index2 = keyList.getSelectedIndex();
				String temp1 = (String)keys.elementAt(index1);
				String temp2 = (String)keys.elementAt(index2);
				
				keys.setElementAt(temp1,index2);
				keys.setElementAt(temp2,index1);
				keyList.setListData(keys);
			}
		});
		keyDown = (JButton) contextEditor.find( "down-key" );
		keyDown.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				if(keys.isEmpty()==true)return;
				int index1 = keyList.getSelectedIndex()+1 > keys.size() ? keys.size() : keyList.getSelectedIndex() + 1;
				int index2 = keyList.getSelectedIndex();
				String temp1 = (String)keys.elementAt(index1);
				String temp2 = (String)keys.elementAt(index2);
				
				keys.setElementAt(temp1,index2);
				keys.setElementAt(temp2,index1);
				keyList.setListData(keys);
			}
		});
		
		rangeSpinner = (JSpinner) contextEditor.find( "range" );
		rangeSpinner.addChangeListener( new ChangeListener()
		{
			public void stateChanged(ChangeEvent changeEvent)
			{
				range = ((Integer)rangeSpinner.getValue()).intValue();
			}
		});
		
		basedirField = (JTextField) contextEditor.find( "basedir" );
		basedirButton = (JButton) contextEditor.find( "change-basedir" );
		basedirButton.addActionListener( new AbstractAction(){
				public void actionPerformed( ActionEvent event ){
					RelativeFileChooser chooser = _animator.getNewFileChooser();
					int ret = chooser.open();
					if ( ret == RelativeFileChooser.OK ){
						final String path = chooser.getPath();
						basedirField.setText( path );
						baseDirectory = path;
					}
				}
			});
		
		eventList = (JList) contextEditor.find( "events");
		eventAdd = (JButton) contextEditor.find( "add-event" );
		eventRemove = (JButton) contextEditor.find( "remove-event" );
		eventUp = (JButton) contextEditor.find( "up-event" );
		eventDown = (JButton) contextEditor.find( "down-event" );
		
		canvas = (JPanel) animEditor.find( "canvas" );
		area = new DrawArea();
		canvas.add(area);
		
		other = (JPanel) animEditor.find( "other" );
		
		context.add((JComponent)contextEditor.getRootComponent());
	}
}
