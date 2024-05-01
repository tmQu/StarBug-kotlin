package intech.co.starbug.model

class BranchModel (
    val id: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = "",
    val name: String = "",
){
    companion object {
        val BRANCHES = listOf<BranchModel>(
            BranchModel("1", 10.762845975764886, 106.68248203041692, "227 Đ. Nguyễn Văn Cừ, Phường 4, Quận 5, Thành phố Hồ Chí Minh", "Branch district 5"),
            BranchModel("1", 10.71302862411907, 106.73690313761412, "1360 Huỳnh Tấn Phát, Phú Mỹ, Quận 7, Thành phố Hồ Chí Minh", "Branch district 7"),
        )
    }
}