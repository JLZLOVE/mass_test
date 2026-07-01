package untiy.exception;
public interface Level {
    //    注册时 ,后台添加不上
    public static final Integer RGEISTER_ADD_NEW_USER_CODE = 1001;
    public static final String RGEISTER_ADD_NEW_USER_MSG = "注册失败,请重新尝试";
    int SUPER_ADMIN = 0;
    int ADMIN = 1;
    int CLUB_LEADER = 2;
    int DEPT_LEADER = 3;
    int STUDENT = 4;
}