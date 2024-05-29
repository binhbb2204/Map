
package model;

import javax.swing.*;

public class Model_Menu {

    private String icon;
    private String name;
    private MenuType type;
    
    public Model_Menu(String icon, String name, MenuType type) {
        this.icon = icon;
        this.name = name;
        this.type = type;
    }
    
    public static enum MenuType {
        TITLE, MENU, EMPTY
    }
    
    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public MenuType getType() {
        return type;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    public Icon toIcon(){
        return new ImageIcon(getClass().getResource("/icons/"+icon+".png"));
    }
    
    
}
