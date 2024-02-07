package com.github.novicezk.midjourney.enums;


/**
 * 任务操作类型
 * @author qinfen
 * @date 2024/01/29
 */
public enum TaskAction {
	/**
	 * 生成图片.
	 */
	IMAGINE,
	/**
	 * 选中放大.
	 */
	UPSCALE,
	/**
	 * 选中其中的一张图，生成四张相似的.
	 */
	VARIATION,
	/**
	 * 重新执行.
	 */
	REROLL,
	/**
	 * 图转prompt.
	 */
	DESCRIBE,
	/**
	 * 多图混合.
	 */
	BLEND,

	/**
	 * 像素放大2倍
	 */
	UPSAMPLE2X,

	/**
	 * 像素放大4倍
	 */
	UPSAMPLE4X,

	/**
	 * 缩放1.5倍并补充细节生成四张图
	 */
	OUTPAINT15X,

	/**
	 * 缩放2倍并补充细节生成四张图
	 */
	OUTPAINT2X,

	/**
	 * 细微变化生成四张图
	 */
	VARIATION_LOW,

	/**
	 * 强烈变化生成四张图
	 */
	VARIATION_HIGH,


	/**
	 * 往上延伸生成四张图
	 */
	DIRECTION_UP,
	/**
	 * 往下延伸生成四张图
	 */
	DIRECTION_DOWN,
	/**
	 * 往左延伸生成四张图
	 */
	DIRECTION_LEFT,
	/**
	 * 往右延伸生成四张图
	 */
	DIRECTION_RIGHT,


}
