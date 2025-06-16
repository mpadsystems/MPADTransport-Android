package com.wneth.mpadtransport.repositories


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.wneth.mpadtransport.models.RoleModel
import com.wneth.mpadtransport.models.UserModel
import com.wneth.mpadtransport.models.UserWithRole


@Dao
interface IUserRepository {
    @Query("SELECT usr.*, role.name AS roleName FROM users AS usr INNER JOIN roles AS role ON role.id = usr.roleId")
    fun getUsers(): List<UserWithRole>?

    @Query("SELECT usr.*, role.name AS roleName FROM users AS usr INNER JOIN roles AS role ON role.id = usr.roleId WHERE usr.id = :id")
    fun getUserInfoById(id:Int): UserWithRole?

    @Query("SELECT id FROM accounts")
    fun getUserIds(): List<Int>?

    @Query("SELECT usr.*, role.name AS roleName FROM users AS usr INNER JOIN roles AS role ON role.id = usr.roleId WHERE usr.id = :id")
    fun getUser(id:Int): UserWithRole?

    @Query("SELECT usr.*, role.name AS roleName FROM users AS usr INNER JOIN roles AS role ON role.id = usr.roleId WHERE usr.roleId = :roleId")
    fun getUsersByRole(roleId:Int): List<UserWithRole>?

    @Query("SELECT usr.*, role.name AS roleName FROM users AS usr INNER JOIN roles AS role ON role.id = usr.roleId WHERE usr.fullName LIKE '%'||:keyword||'%' OR role.name LIKE '%'||:keyword||'%'")
    fun filterUsers(keyword:String): List<UserWithRole>?

    @Insert
    fun insertUser(user: UserModel)

    @Insert
    fun insertUserBulk(users: List<UserModel>): LongArray

    @Update
    fun updateUser(user: UserModel)

    @Delete
    fun deleteUser(user: UserModel)

    @Query("DELETE FROM users")
    fun deleteAllUser()

    @Query("DELETE FROM sqlite_sequence WHERE name='users'")
    fun resetAutoIncrementUser()



    //====ROLES
    @Query("SELECT * FROM roles")
    fun getRoles(): List<RoleModel>


    @Query("SELECT * FROM roles WHERE id = :id")
    fun getRole(id:Int): RoleModel?

    /*@Query("SELECT * FROM terminals WHERE name LIKE '%'||:keyword||'%' OR address LIKE '%'||:keyword||'%'")
    fun filterTerminals(keyword:String): List<TerminalModel>?*/

    @Insert
    fun insertRole(role: RoleModel)

    @Insert
    fun insertRoleBulk(roles: List<RoleModel>): LongArray

    @Delete
    fun deleteRole(role: RoleModel)

    @Query("DELETE FROM roles")
    fun deleteAllRole()

    @Query("DELETE FROM sqlite_sequence WHERE name='roles'")
    fun resetAutoIncrementRole()
}



