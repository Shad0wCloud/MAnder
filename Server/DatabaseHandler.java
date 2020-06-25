package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.*;

public class DatabaseHandler extends Configs {
    static Connection dbConnection;

    public static Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?verifyServerCertificate=false" +
                "&useSSL=false" +
                "&requireSSL=false" +
                "&useLegacyDatetimeCode=false" +
                "&amp" +
                "&serverTimezone=UTC";

        Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);

        return dbConnection;
    }

    public static String singUpUser(String login, String password, String name, String phone, String gender) throws ClassNotFoundException {
        String result = "accept";
        String insertreg = "INSERT INTO users(" + Const.USER_LOGIN + "," + Const.USER_PASSWORD + "," + Const.USER_NAME +
                ",phone,gender)" + "VALUES(?,?,?,?,?)";
        try {
            PreparedStatement pr = getDbConnection().prepareStatement(insertreg);
            pr.setString(1, login);
            pr.setString(2, password);
            pr.setString(3, name);
            pr.setString(4, phone);
            pr.setString(5, gender);
            pr.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e){
            e.printStackTrace();
            result = "denied";
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String insert ="INSERT INTO userprofile VALUE(profile_id,'" + name+ "',NULL,NULL,0,NULL,'src/server/img/No_photo.png',0)";
            PreparedStatement createProfile = getDbConnection().prepareStatement(insert);
            createProfile.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean CheckLogin(String loginfield, String passwordfield) {
        boolean result = false;
        int count = 0;
        try {
            Statement statement = getDbConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                String login = resultSet.getString(2);
                String password = resultSet.getString(3);
                if (loginfield.equals(login) && passwordfield.equals(password)) {
                    count++;
                } else {
                    //do nothing
                }
            }
            if (count == 1) {
                result = true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static int GetId(String loginfield, String passwordfield){
        int id =-1;
        try {
            Statement statement = getDbConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                String login = resultSet.getString(2);
                String password = resultSet.getString(3);
                id = resultSet.getInt(1);
                if (loginfield.equals(login) && passwordfield.equals(password)) {
                    break;
                } else {

                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return id;
    }
    public static void ProfileInfo(String name, String country , String city, String age,String info,String destfoto,int id){
        String insert ="UPDATE userprofile set name ='"+name+
                "',country = '"+country+
                "', city ='"+city+
                "', age="+age+
                ", info='"+info+
                "', picture='"+destfoto+
                "' WHERE profile_id="+id+";";
        try {
            PreparedStatement ps = getDbConnection().prepareStatement(insert);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public static String Getfoto(int profId){
        String destenfoto = null;
        try {
            Statement statement = getDbConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM userprofile where profile_id ='" + profId + "'");
            System.out.println("SELECT * FROM userprofile where profile_id ='" + profId + "'");
            while (resultSet.next()) {
                destenfoto = resultSet.getString(7);
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return destenfoto;
    }
    public Integer Size () {
        int size = 0;
        try {
            Statement statement = getDbConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM userprofile");
            while (resultSet.next()) {
                size++;
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return size;
    }
    public String PostInfo (/*String loginUser,*/ int profId){
        String infopost = null;
        String phone = "";
        String gender ="";
        try {
            Statement statement1 = getDbConnection().createStatement();
            ResultSet resultSet1 = statement1.executeQuery("SELECT * FROM users where id ='" + profId + "'");
            while (resultSet1.next()) {
                phone = resultSet1.getString(5);
                gender = resultSet1.getString(6);
            }
            Statement statement = getDbConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM userprofile where profile_id ='" + profId + "'");
            while (resultSet.next()) {
                String name = resultSet.getString(2);
                String country = resultSet.getString(3);
                String city = resultSet.getString(4);
                int age = resultSet.getInt(5);
                String info = resultSet.getString(6);
                String likes = resultSet.getString(8);
                infopost = ("Name: "+ name
                        +"\nGender: "+gender
                        +"\nCountry: "+ country
                        +"\nCity: "+ city
                        +"\nAge: "+ age
                        +"\nPhone: "+phone
                        +"\nInfo: "+ info
                        +"!!razdel!!"+likes+"!!razdel!!");
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return infopost;
    }
    public static void GetLike(int id,int value){
        String insert ="UPDATE userprofile set likes ="+value+" WHERE profile_id="+id+";";
        try {
            PreparedStatement ps = getDbConnection().prepareStatement(insert);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public static String getUser(int id) {
        ResultSet resset = null;
        String profile ="";
        String select = "SELECT * FROM userprofile WHERE profile_id ="+id+";";
        try {
            PreparedStatement pr1 = getDbConnection().prepareStatement(select);
            resset = pr1.executeQuery();
            while (resset.next()) {
                String name = resset.getString(2);
                String coutry = resset.getString(3);
                String city = resset.getString(4);
                int age = resset.getInt(5);
                String info = resset.getString(6);
                profile = name +"!!razdel!!" + coutry + "!!razdel!!" + city + "!!razdel!!" + age + "!!razdel!!" + info + "!!razdel!!";
                break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return profile;
    }
    public static void SingUpIp(String ip) throws SQLException, ClassNotFoundException {
        String insert ="INSERT INTO ipusers(ip,port)"+"VALUES(?,?)";
        PreparedStatement pr = getDbConnection().prepareStatement(insert);
        pr.setString(1, ip);
        pr.setInt(2,7770);
        pr.executeUpdate();
    }
    public static void ChangeIp(String ip, int id) throws SQLException, ClassNotFoundException {
        String insert ="UPDATE ipusers SET IP = '"+ip+"' where id="+id+";";
        PreparedStatement pr = getDbConnection().prepareStatement(insert);
        pr.executeUpdate();
    }
    public static void SingUpMSG(String name, String msg) throws SQLException, ClassNotFoundException {
        String insert = "insert into messages value('" + name + "',current_time(),'" + msg + "');";
        PreparedStatement ps = getDbConnection().prepareStatement(insert);
        ps.executeUpdate();
    }
    public static String[] GetIps(int send, int reciver) throws SQLException, ClassNotFoundException {
        String[] ips = {"","","",""};
        String select = "SELECT * FROM ipusers WHERE id ="+send+";";
        PreparedStatement pr1 = getDbConnection().prepareStatement(select);
        ResultSet resset = pr1.executeQuery();
        while (resset.next()) {
            ips[0] = resset.getString(2);
            ips[1] +=resset.getString(3);
        }
        String select2 = "SELECT * FROM ipusers WHERE id ="+reciver+";";
        PreparedStatement pr2 = getDbConnection().prepareStatement(select2);
        ResultSet resset2 = pr2.executeQuery();
        while (resset2.next()) {
            ips[2] = resset2.getString(2);
            ips[3] +=resset2.getString(3);
        }
        return ips;
    }
}
