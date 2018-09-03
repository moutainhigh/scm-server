package org.trc.enums;

public class AfterSaleOrderEnum {

	//售后单状态
	public enum AfterSaleOrderStatusEnum {
		
		STATUS_0(0,"待客户发货"),
		STATUS_1(1,"客户已经发货"),
		STATUS_3(2,"已经完成"),
		STATUS_4(3,"已经取消");

		private int code;
		private String name;

		AfterSaleOrderStatusEnum(int code, String name) {
			this.code = code;
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}
		
	}
	
	//售后单详情
	public enum AfterSaleOrderDetailTypeEnum {
		
		STATUS_0(0,"自采商品"),
		STATUS_1(1,"代发商品");

		private int code;
		private String name;

		AfterSaleOrderDetailTypeEnum(int code, String name) {
			this.code = code;
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}
		
	}
	
	//退货入库单状态
		public enum AfterSaleWarehouseNoticeStatusEnum {
			
			STATUS_0(0,"未到货"),
			STATUS_1(1,"已到货待理货 "),
			STATUS_2(2,"入库完成"),
			STATUS_3(3,"已取消");

			private int code;
			private String name;  

			AfterSaleWarehouseNoticeStatusEnum(int code, String name) {
				this.code = code;
				this.name = name;
			}
			
			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public int getCode() {
				return code;
			}

			public void setCode(int code) {
				this.code = code;
			}
			
		}
}
