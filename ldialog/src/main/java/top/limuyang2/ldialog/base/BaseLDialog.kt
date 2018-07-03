package top.limuyang2.ldialog.base


import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.FloatRange
import android.support.annotation.LayoutRes
import android.support.annotation.StyleRes
import android.support.v4.app.FragmentManager
import android.view.*
import kotlinx.android.parcel.Parcelize
import top.limuyang2.ldialog.R

/**
 *
 * Date 2018/6/26
 * @author limuyang
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseLDialog<T : BaseLDialog<T>> : android.support.v4.app.DialogFragment() {

    protected var baseParams: BaseDialogParams

    protected var viewHandlerListener: ViewHandlerListener?

    private var onDialogDismissListener: OnDialogDismissListener? = null

    private lateinit var mContext: Context

    init {
        baseParams = BaseDialogParams().apply {
            layoutRes = layoutRes()
            view = layoutView()
        }
        viewHandlerListener = this.viewHandler()
    }

    @LayoutRes
    protected abstract fun layoutRes(): Int

    protected abstract fun layoutView(): View?

    protected abstract fun viewHandler(): ViewHandlerListener?

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            //恢复UI状态
            baseParams = it.getParcelable(KEY_PARAMS)
            viewHandlerListener = savedInstanceState.getParcelable(KEY_VIEW_HANDLER)
            onDialogDismissListener = savedInstanceState.getParcelable(KEY_DISMISS_LISTENER)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return when {
            baseParams.layoutRes > 0 -> inflater.inflate(baseParams.layoutRes, container)
            baseParams.view != null -> baseParams.view!!
            else ->
                throw IllegalArgumentException("请先设置LayoutRes或View!")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewHandlerListener?.convertView(ViewHolder.create(view), this)
    }

    //save UI state
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_PARAMS, baseParams)
        outState.putParcelable(KEY_VIEW_HANDLER, viewHandlerListener)
        outState.putParcelable(KEY_DISMISS_LISTENER, onDialogDismissListener)
    }

    override fun onStart() {
        super.onStart()

        val point = Point()
        val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.defaultDisplay?.getSize(point)

        dialog.window?.let {
            val params = it.attributes
            params.gravity = baseParams.gravity

            //set dialog width
            when {
                baseParams.widthScale > 0f -> {
                    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && !baseParams.keepWidthScale) {
                        //横屏并且不保持比例
                        params.width = WindowManager.LayoutParams.WRAP_CONTENT
                    } else {
                        params.width = (point.x * baseParams.widthScale).toInt()
                    }
                }
                baseParams.widthDp > 0f -> params.width = dp2px(mContext, baseParams.widthDp)

                else -> params.width = WindowManager.LayoutParams.WRAP_CONTENT
            }

            //set dialog height
            when {
                baseParams.heightScale > 0f -> {
                    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && !baseParams.keepHeightScale) {
                        //横屏并且不保持比例
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    } else {
                        params.height = (point.y * baseParams.heightScale).toInt()
                    }
                }
                baseParams.heightDp > 0f -> params.height = dp2px(mContext, baseParams.heightDp)

                else -> params.height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            it.attributes = params
            it.setBackgroundDrawableResource(baseParams.backgroundDrawableRes)
            it.setWindowAnimations(baseParams.animStyle)
        }

        //set touch cancelable
        if (!baseParams.cancelable) {
            isCancelable = baseParams.cancelable
        } else {
            dialog.setCanceledOnTouchOutside(baseParams.cancelableOutside)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        System.out.println("onDismiss")
        onDialogDismissListener?.onDismiss(dialog)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        dismiss()
    }

    protected fun setFragmentManager(fragmentManager: FragmentManager) {
        baseParams.fragmentManager = fragmentManager
    }

    /*** Set Params  (start)***/
    fun setTag(tag: String): T {
        baseParams.tag = tag
        return this as T
    }

    fun setDismissListener(onDialogDismissListener: OnDialogDismissListener): T {
        this.onDialogDismissListener = onDialogDismissListener
        return this as T
    }

    fun setWidthScale(@FloatRange(from = 0.0, to = 1.0) scale: Float): T {
        baseParams.widthScale = scale
        return this as T
    }

    fun setWidthDp(dp: Float): T {
        baseParams.widthDp = dp
        return this as T
    }

    fun setHeightScale(@FloatRange(from = 0.0, to = 1.0) scale: Float): T {
        baseParams.heightScale = scale
        return this as T
    }

    fun setHeightDp(dp: Float): T {
        baseParams.heightDp = dp
        return this as T
    }

    /**
     * 当屏幕旋转后，是否保持所设置的宽度比例
     * 默认不保持
     * @param isKeep Boolean
     * @return T
     */
    fun isKeepWidthScale(isKeep: Boolean): T {
        baseParams.keepWidthScale = isKeep
        return this as T
    }

    /**
     * 当屏幕旋转后，是否保持所设置的高度比例
     * 默认不保持
     * @param isKeep Boolean
     * @return T
     */
    fun isKeepHeightScale(isKeep: Boolean): T {
        baseParams.keepHeightScale = isKeep
        return this as T
    }

    fun setCancelableAll(cancelable: Boolean): T {
        baseParams.cancelable = cancelable
        return this as T
    }


    fun setCancelableOutside(cancelableOutside: Boolean): T {
        baseParams.cancelableOutside = cancelableOutside
        return this as T
    }

    fun setBackgroundDrawableRes(@DrawableRes resId: Int): T {
        baseParams.backgroundDrawableRes = resId
        return this as T
    }

    fun setAnimStyle(@StyleRes animStyleRes: Int): T {
        baseParams.animStyle = animStyleRes
        return this as T
    }

//    fun setInAnimation(animation: Animation): T {
//
//        return this as T
//    }
//
//    fun setOutAnimation(animation: Animation): T {
//
//        return this as T
//    }

    fun show(): T {
        show(baseParams.fragmentManager, baseParams.tag)
        return this as T
    }

    /*** Set Params  (end)***/

    companion object {
        private const val KEY_PARAMS = "key_params"
        private const val KEY_VIEW_HANDLER = "view_handler"
        private const val KEY_DISMISS_LISTENER = "dismiss_listener"

        private fun dp2px(context: Context, dipValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }

    }

    abstract class UnParcelableParams(var fragmentManager: FragmentManager? = null,
                                      var view: View? = null)

    @Parcelize
    class BaseDialogParams(
            @LayoutRes var layoutRes: Int = 0,
            var widthScale: Float = 0f,
            var widthDp: Float = 0f,

            var heightScale: Float = 0f,
            var heightDp: Float = 0f,
            var keepWidthScale: Boolean = false,
            var keepHeightScale: Boolean = false,

            var gravity: Int = Gravity.CENTER,
            var tag: String = "rgDialog",
            var cancelable: Boolean = true,
            var cancelableOutside: Boolean = true,
            var backgroundDrawableRes: Int = R.drawable.def_dialog_bg,
            var animStyle: Int = 0
    ) : UnParcelableParams(), Parcelable

}