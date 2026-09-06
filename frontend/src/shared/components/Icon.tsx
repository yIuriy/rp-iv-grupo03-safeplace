import type { CSSProperties } from 'react'
import plus from '../../../design-system/assets/icons/plus.svg'
import check from '../../../design-system/assets/icons/check.svg'
import x from '../../../design-system/assets/icons/x.svg'
import info from '../../../design-system/assets/icons/info.svg'
import alertTriangle from '../../../design-system/assets/icons/alert-triangle.svg'
import search from '../../../design-system/assets/icons/search.svg'
import chevronDown from '../../../design-system/assets/icons/chevron-down.svg'
import chevronRight from '../../../design-system/assets/icons/chevron-right.svg'
import arrowLeft from '../../../design-system/assets/icons/arrow-left.svg'
import calendar from '../../../design-system/assets/icons/calendar.svg'
import paperclip from '../../../design-system/assets/icons/paperclip.svg'
import clipboard from '../../../design-system/assets/icons/clipboard.svg'
import packageIcon from '../../../design-system/assets/icons/package.svg'
import mapPin from '../../../design-system/assets/icons/map-pin.svg'
import users from '../../../design-system/assets/icons/users.svg'
import moreHorizontal from '../../../design-system/assets/icons/more-horizontal.svg'
import filter from '../../../design-system/assets/icons/filter.svg'
import loader from '../../../design-system/assets/icons/loader.svg'
import checkCircle from '../../../design-system/assets/icons/check-circle.svg'

const icons = {
  plus, check, x, info, 'alert-triangle': alertTriangle, search,
  'chevron-down': chevronDown, 'chevron-right': chevronRight,
  'arrow-left': arrowLeft, calendar, paperclip, clipboard,
  package: packageIcon, 'map-pin': mapPin, users,
  'more-horizontal': moreHorizontal, filter, loader, 'check-circle': checkCircle,
}

export type IconName = keyof typeof icons

export function Icon({ name, label, className = '' }: {
  name: IconName
  label?: string
  className?: string
}) {
  return <span
    className={`sp-icon ${className}`}
    style={{ '--sp-icon-url': `url("${icons[name]}")` } as CSSProperties}
    role={label ? 'img' : undefined}
    aria-label={label}
    aria-hidden={label ? undefined : true}
  />
}
