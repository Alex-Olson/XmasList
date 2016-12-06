/**
 * Created by Nnamdi on 12/4/2016.
 */

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XmasDataModel extends AbstractTableModel {
    ResultSet resultSet;
    private int rowCount = 0;
    private int colCount = 0;

    public XmasDataModel ( ResultSet rs ) {
        this.resultSet = rs;
        setup ( );
    }

    private void setup () {

        countRows ( );

        try {
            colCount = resultSet.getMetaData ( ).getColumnCount ( );

        } catch (SQLException se) {
            System.out.println ( "Error counting columns" + se );
        }

    }


    public void updateResultSet ( ResultSet newRS ) {
        resultSet = newRS;
        setup ( );
    }


    private void countRows () {
        rowCount = 0;
        try {
            //Move cursor to the start...
            resultSet.beforeFirst ( );
            // next() method moves the cursor forward one row and returns true if there is another row ahead
            while ( resultSet.next ( ) ) {
                rowCount++;

            }
            resultSet.beforeFirst ( );

        } catch (SQLException se) {
            System.out.println ( "Error counting rows " + se );
        }

    }

    @Override
    public int getRowCount () {
        countRows ( );
        return rowCount;
    }

    @Override
    public int getColumnCount () {
        return colCount;
    }

    @Override
    public Object getValueAt ( int row, int col ) {
        try {
            //  System.out.println("get value at, row = " +row);
            resultSet.absolute ( row + 1 );
            Object o = resultSet.getObject ( col + 1 );
            return o.toString ( );
        } catch (SQLException se) {
            System.out.println ( se );
            //se.printStackTrace();
            return se.toString ( );

        }
    }

    @Override
    //This is called when user edits an editable cell
    public void setValueAt ( Object newValue, int row, int col ) {

        //Make sure newValue is an integer AND that it is in the range of valid ratings

        int newRating;

        try {
            newRating = Integer.parseInt ( newValue.toString ( ) );

            if ( newRating < XmasDB.MOVIE_MIN_RATING || newRating > XmasDB.MOVIE_MAX_RATING ) {
                throw new NumberFormatException ( "Movie rating must be within the valid range" );
            }
        } catch (NumberFormatException ne) {
            //Error dialog box. First argument is the parent GUI component, which is only used to center the
            // dialog box over that component. We don't have a reference to any GUI components here
            // but are allowed to use null - this means the dialog box will show in the center of your screen.
            JOptionPane.showMessageDialog ( null, "Try entering a number between " + XmasDB.MOVIE_MIN_RATING + " " + XmasDB.MOVIE_MAX_RATING );
            //return prevents the following database update code happening...
            return;
        }

        //This only happens if the new rating is valid
        try {
            resultSet.absolute ( row + 1 );
            resultSet.updateInt ( XmasDB.PRIORITY_COLUMN, newRating );
            resultSet.updateRow ( );
            fireTableDataChanged ( );
        } catch (SQLException e) {
            System.out.println ( "error changing priority " + e );
        }

    }


    @Override
    public boolean isCellEditable ( int row, int col ) {
        return col == 3;
    }

    //Delete row, return true if successful, false otherwise
    public boolean deleteRow ( int row ) {
        try {
            resultSet.absolute ( row + 1 );
            resultSet.deleteRow ( );
            //Tell table to redraw itself
            fireTableDataChanged ( );
            return true;
        } catch (SQLException se) {
            System.out.println ( "Delete row error " + se );
            return false;
        }
    }

    //returns true if successful, false if error occurs
    public boolean insertRow ( String name, int price, int priority ) {

        try {
            //Move to insert row, insert the appropriate data in each column, insert the row, move cursor back to where it was before we started
            resultSet.moveToInsertRow ( );
            resultSet.updateString ( XmasDB.NAME_COLUMN, name );
            resultSet.updateInt ( XmasDB.PRICE_COLUMN, price );
            resultSet.updateInt ( XmasDB.PRIORITY_COLUMN, priority );
            resultSet.insertRow ( );
            resultSet.moveToCurrentRow ( );
            fireTableDataChanged ( );
            return true;

        } catch (SQLException e) {
            System.out.println ( "Error adding row" );
            System.out.println ( e );
            return false;
        }

    }

    @Override
    public String getColumnName ( int col ) {
        try {
            return resultSet.getMetaData ( ).getColumnName ( col + 1 );
        } catch (SQLException se) {
            System.out.println ( "Error fetching column names" + se );
            return "?";
        }
    }

}
